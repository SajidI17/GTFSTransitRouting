package dev.sajidislam.util;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.*;
import com.opencsv.CSVWriter;
import java.io.FileWriter;
import java.util.Date;

// Monday - 0, Sunday - 7
//todo: convert string time to java LocalTime

public class Main {
    private static final String URL = "jdbc:postgresql://localhost:5432/OCDatabase"; //Change OCGTFS to the name of the database you have
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "admin";
    private static Map<String, Boolean> serviceTypeMap;
    private static Graph busNetwork;
    private static List<String> excludeTripIds;
    private static Array serviceSqlArray;

    public static void main(String[] args) {
        //7851 - orleans
        //1278 - kanata
        //0835
        //3052 - parliament
        //3062 - carleton
        long startTime = System.nanoTime();
        List<BusStopWeb> busStopList = runProgram("1278", "3062","09:45:00", 20250715);

        long endTime = System.nanoTime();
        long totalRunTime = (endTime - startTime) / 1000000;
        System.out.println("\nTOTAL RUNNING TIME OF ALGORITHM: " + totalRunTime + " milliseconds");
    }

    public static List<BusStopWeb> runProgram(String busStopOrigin, String busStopDestination, String time, int date){

        serviceTypeMap = new HashMap<>();
        busNetwork = new Graph();
        excludeTripIds = new ArrayList<>();
        excludeTripIds.add("");
        try{
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);

            //convert date to day of the week
            Date javaDate = new SimpleDateFormat("yyyyMMdd").parse(String.valueOf(date));
            String dayOfWeek = new SimpleDateFormat("EEEE").format(javaDate);
            System.out.println(dayOfWeek);

            setSchedules(connection, date, dayOfWeek);
            serviceSqlArray = connection.createArrayOf("TEXT", serviceTypeMap.keySet().toArray());

            List<BusStop> busStopList = createTopologicalGraph(busStopOrigin, busStopDestination, time, date, connection);
            List<BusStop> optimizeBusStopList = optimizeBusRoute(busStopList, time, connection);
            for(BusStop busStop : optimizeBusStopList){
                System.out.println(busStop);
            }
            List<BusStopWeb> busStopWebList = convertForWeb(optimizeBusStopList, connection);
            connection.close();
            return busStopWebList;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /// Sets the static variable serviceTypeMap with the serviceIDs that run on the user requested date
    public static void setSchedules(Connection connection, int date, String dayOfWeek){
        try{

            // get all service_ids for a given date
            String sqlStatement = "SELECT * FROM calendar";
            PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()){
                // get weekday of services for that serviceId
                String serviceId = resultSet.getString("service_id");
                int serviceStartDate = resultSet.getInt("start_date");
                int serviceEndDate = resultSet.getInt("end_date");
                boolean serviceDayOfWeek = resultSet.getBoolean(dayOfWeek.toLowerCase());

                if(serviceDayOfWeek && serviceStartDate <= date && date <= serviceEndDate){
                    serviceTypeMap.putIfAbsent(resultSet.getString("service_id"), true);
                }
            }
            sqlStatement = "SELECT * FROM calendar_dates WHERE date = ?";
            preparedStatement = connection.prepareStatement(sqlStatement);
            preparedStatement.setInt(1, date);
            resultSet = preparedStatement.executeQuery();

            while(resultSet.next()){
                int exceptionType = resultSet.getInt("exception_type");
                String serviceExceptionId = resultSet.getString("service_id");
                if(exceptionType == 2){
                    serviceTypeMap.remove(serviceExceptionId);
                }
                else {
                    serviceTypeMap.putIfAbsent(serviceExceptionId, true);
                }
            }



        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /// Prints compatible dates available in the database, mainly used for UI
    public static List<Integer> getAvailableDates(){
        List<Integer> dates = new ArrayList<>();

        try{
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);

            String sqlStatement = "SELECT DISTINCT date FROM calendar_dates ORDER BY date ASC";
            PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement);
            ResultSet resultSet = preparedStatement.executeQuery();

            while(resultSet.next()){
                dates.add(resultSet.getInt("date"));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return dates;
    }

    /// Converts a bus stop to the StopId used in the database
    public static String convertCodeToId(String busStopCode, Connection connection){
        try {
            String sqlStatement = "SELECT stop_id FROM stops WHERE stop_code = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement);
            preparedStatement.setString(1, busStopCode);
            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next()){
                return resultSet.getString("stop_id");
            }
            else{
                System.out.println("Fail!");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return "";
    }

    /// Given the date and time, returns all bus stops leaving within 20 minutes from that bus stop
    public static List<BusRecord> getAllDepartingBusses(String busStopId, String time, int minutes, Connection connection){
        List<BusRecord> BusRecordList = new ArrayList<>();
        try{

            //set sql array
            Array tripsSqlArr = connection.createArrayOf("TEXT", excludeTripIds.toArray());

            //get all buses leaving a particular stop within x minutes of time AND has the correct serviceID
            //String sqlStatement = "SELECT * FROM stop_times WHERE stop_Id = ? AND arrival_time > (?::interval) AND arrival_time <= (?::interval + INTERVAL '"+ minutes + " minutes') AND NOT (trip_id = ANY(?))";
            String sqlStatement = "SELECT * FROM stop_times as s INNER JOIN trips ON s.trip_id = trips.trip_id WHERE s.stop_Id = ? AND s.arrival_time > (?::interval) AND s.arrival_time <= (?::interval + INTERVAL '" + minutes + " minutes') AND NOT (s.trip_id = ANY(?)) AND (trips.service_id = ANY(?))";
            PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement);
            preparedStatement.setString(1,busStopId);
            preparedStatement.setObject(2, time);
            preparedStatement.setObject(3, time);
            preparedStatement.setArray(4, tripsSqlArr);
            preparedStatement.setArray(5, serviceSqlArray);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String tripId = resultSet.getString("trip_id");

                String arrivalTime = resultSet.getTime("arrival_time").toString();
                String stopId = resultSet.getString("stop_id");
                String routeId = resultSet.getString("route_id");
                int stopSequence = resultSet.getInt("stop_sequence");
                String headSign = resultSet.getString("trip_headsign");
                String serviceId = resultSet.getString("service_id");
                BusRecordList.add(new BusRecord(arrivalTime, stopId, tripId, routeId, stopSequence, headSign, serviceId));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return BusRecordList;
    }
    /// Gets the routeId when given the tripId, routeId identifies the bus.
    /// NOTE: This probably can be made more efficient.
    /// might be worth looking into storing route_id in the stop_times table
    public static String getRouteIdFromTripId(String tripId, Connection connection){
        String result = "";
        try {
            String sqlStatement = "SELECT * FROM trips WHERE trip_id = ? LIMIT 1";
            PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement);
            preparedStatement.setString(1,tripId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                return resultSet.getString("route_id");
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public static Map<String, BusStop> visitingBusStops(List<BusRecord> busRecordList, Map<String, BusStop> busStopUpdate, Connection connection){
        try {
            for (BusRecord busRecord : busRecordList){
                //get the list of bus stops the bus is visiting
                String sqlStatement = "SELECT * FROM stop_times WHERE trip_id = ? AND stop_sequence >= ?";
                PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement);
                preparedStatement.setString(1,busRecord.tripId);
                preparedStatement.setInt(2,busRecord.stopSequence);

                BusStop prevStop = null;
                ResultSet resultSet = preparedStatement.executeQuery();

                //will store current routeId of the trip, will reduce number of queries
                String currentRouteId = "";

                //sets the previous bus stop
                if (resultSet.next()){
                    String stopId = resultSet.getString("stop_id");
                    String tripId = resultSet.getString("trip_id");
                    currentRouteId = getRouteIdFromTripId(tripId, connection);
                    String arrivalTime = resultSet.getTime("arrival_time").toString();

                    prevStop = new BusStop(stopId,tripId,currentRouteId,arrivalTime);
                }

                while(resultSet.next()){
                    String stopId = resultSet.getString("stop_id");
                    String tripId = resultSet.getString("trip_id");
                    String arrivalTime = resultSet.getTime("arrival_time").toString();

                    //as we are iterating through a trip, we know the routeId has not changed
                    BusStop curStop = new BusStop(stopId,tripId,currentRouteId,arrivalTime);
                    //we know that the prevStop variable will be defined due to resultSet.next()

                    assert prevStop != null;
                    //if the curStop does not exist, then can create an edge to curStop
                    //as curStop did not exist in graph, it did not have any incoming edges, thus edge can be added without issue

                    //if something is added to graph then
                    //1. append to busStopsAdded list
                    //2. set prevStop = curStop;
                    boolean isAdded = graphAddBusStopCheck(prevStop, curStop);
                    if(isAdded){
                        busStopUpdate.put(curStop.stopCodeId,curStop);
                        //busStopsAdded.add(curStop);
                        prevStop = curStop;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return busStopUpdate;
    }

    public static boolean graphAddBusStopCheck(BusStop prevStop, BusStop curStop){
        //if the curStop does not exist, then can create an edge to curStop
        //as curStop did not exist in graph, it did not have any incoming edges, thus edge can be added without issue
        if(!busNetwork.doesNodeExist(curStop.stopCodeId)){
            //set what the previous stop is
            curStop.previousStopId = (prevStop.stopCodeId);

            //add edge
            busNetwork.addEdge(prevStop,curStop,stringTimeDifferences(prevStop.arrivalTime, curStop.arrivalTime));

            return true;
        }
        else{
            //curStop already exists in graph

            //get the current nodes saved in the graph
            BusStop currentStop = busNetwork.getBusStop(curStop.stopCodeId);

            //todo: this is a temporarily fix, accounting for first BusStop which would have no previousStopId set
            if(currentStop.previousStopId == null){
                return false;
            }
            BusStop previousStop = busNetwork.getBusStop(currentStop.previousStopId);

            boolean isEarlier = isTimeOneEarlier(curStop.arrivalTime, currentStop.arrivalTime);
            if(isEarlier){
                //as the stop is earlier, we need to adjust edges

                // remove the old edge that was connecting to the current stop
                busNetwork.removeEdge(previousStop.stopCodeId,currentStop.stopCodeId);

                //update new previous stop for curStop
                curStop.previousStopId = prevStop.stopCodeId;

                //update the current bus stop with the new information
                busNetwork.allBusStops.put(currentStop.stopCodeId, curStop);

                //create new edge to current bus stop
                busNetwork.addEdge(prevStop,curStop,stringTimeDifferences(prevStop.arrivalTime, curStop.arrivalTime));

                return true;
            }
        }
        return false;
    }



    public static BusStop convertToBusStopNode(ResultSet resultSet, Connection connection){
        BusStop busStop = null;
        try{
            String stopId = resultSet.getString("stop_id");
            String tripId = resultSet.getString("trip_id");
            String routeId = getRouteIdFromTripId(tripId, connection);
            String arrivalTime = resultSet.getTime("arrival_time").toString();

            busStop = new BusStop(stopId,tripId,routeId,arrivalTime);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return busStop;
    }

    public static long stringTimeDifferences(String time1, String time2){
        LocalTime t1 = LocalTime.parse(time1);
        LocalTime t2 = LocalTime.parse(time2);

        return Duration.between(t1,t2).toSeconds();
    }

    /// returns true if the first time parameter is earlier than the second time parameter
    public static boolean isTimeOneEarlier(String timeOne, String timeTwo){
        LocalTime t1 = LocalTime.parse(timeOne);
        LocalTime t2 = LocalTime.parse(timeTwo);

        return t1.isBefore(t2);
    }

    /// given a list of bus stops, finds transfers between different bus stops within 0.15km of each other
    public static Map<String, BusStop> findTransfers(Map<String, BusStop> busStopUpdate, Connection connection){
        List<BusStop> busStopList = new ArrayList<>();
        try{
            for (BusStop busStop : busStopUpdate.values()){
                String sqlStatement = "SELECT CASE WHEN stop_id_start = ? THEN stop_id_end ELSE stop_id_start END as stop_id, time_in_minutes FROM transfers WHERE stop_id_start = ? OR stop_id_end = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement);
                preparedStatement.setString(1,busStop.stopCodeId);
                preparedStatement.setString(2,busStop.stopCodeId);
                preparedStatement.setString(3,busStop.stopCodeId);
                ResultSet resultSet = preparedStatement.executeQuery();

                while(resultSet.next()){
                    String stopId = resultSet.getString("stop_id");
                    int travelTime = resultSet.getInt("time_in_minutes");

                    //specify pattern for local time
                    DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
                    LocalTime t1 = LocalTime.parse(busStop.arrivalTime);
                    String newArrivalTime = t1.plusMinutes(travelTime).format(timeFormat);

                    BusStop curStop = new BusStop(stopId,"Walking","Walking",newArrivalTime,busStop.stopCodeId);
                    boolean isAdded = graphAddBusStopCheck(busStop,curStop);

                    if(isAdded){
                        busStopList.add(curStop);
                        //busStopsAdded.add(curStop);
                    }
                }
            }
            for(BusStop busStop : busStopList){
                busStopUpdate.put(busStop.stopCodeId, busStop);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return busStopUpdate;
    }

    /// Generates the graph that will be used to find route
    public static List<BusStop> createTopologicalGraph(String busStopOrigin, String busStopDestination, String time, int date, Connection connection){
        List<BusStop> busStopList = new ArrayList<>();
        try{
            //convert stopId to something usable
            String busStopOriginId = convertCodeToId(busStopOrigin, connection);
            String busStopDestinationId = convertCodeToId(busStopDestination,connection);

            //get all buses leaving within 20 minutes of time (each record is a single bus leaving that particular stop)
            List<BusRecord> busArrivals = getAllDepartingBusses(busStopOriginId, time, 30, connection);

            //debug
            for (BusRecord busArrival : busArrivals) {
                System.out.println(busArrival.toString());
            }

            Map<String, BusStop> busStopUpdate = new HashMap<>();

            //get the bus stops the bus is visiting and add to the graph
            //busStopList records all bus stops that were added to the graph in an iteration
            visitingBusStops(busArrivals, busStopUpdate, connection);

            //get walking transfers and add to the graph
            //also add bus stops added to the list
            findTransfers(busStopUpdate, connection);


            boolean routeNotFoundCheck = true;
            for(int i = 0;i < 2; i++){
                Map<String, BusStop> tempBusList = new HashMap<>();
                //for each bus stop added to graph, find if any of the bus stops has a trip not yet added to the graph
                List<BusRecord> tempBusArrivals = new ArrayList<>();
                for (BusStop busStop : busStopUpdate.values()){
                    //get all departing buses from a bus stop recently added to graph
                    tempBusArrivals.addAll(getAllDepartingBusses(busStop.stopCodeId, busStop.arrivalTime, 15, connection));
                }
                //add bus stops to graph, maintain a list containing bus stops added to the graph
                visitingBusStops(tempBusArrivals, tempBusList, connection);
                //get walking transfers, also add to list
                findTransfers(tempBusList,connection);

                busStopUpdate = tempBusList;

                //better way to do this, but if the route was not found yet, try searching one more time
                if(!(busNetwork.doesNodeExist(busStopDestinationId)) && routeNotFoundCheck && i == 1){
                    i = 0;
                    routeNotFoundCheck = false;
                }
            }

            System.out.println("Graph allBusStops size: " + busNetwork.allBusStops.size() + "\nGraph allBusStops size: " + busNetwork.adjacencyList.size());
            if(busNetwork.doesNodeExist(busStopDestinationId)){
                BusStop resultStop = busNetwork.getBusStop(busStopDestinationId);
                while(resultStop.previousStopId != null){
                    busStopList.add(resultStop);
                    resultStop = busNetwork.getBusStop(resultStop.previousStopId);
                }
                //resultStop.previousStopId = resultStop.stopCodeId;
                busStopList.add(resultStop);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return busStopList;
    }

    public static void createResultCSVFile(List<String[]> dataList, String fileName){
        String fileNamePath = "./" + fileName + ".csv";
        try{
            CSVWriter writer = new CSVWriter(new FileWriter(fileNamePath));
            String[] headerLine = {"stopCodeId", "tripId", "routeId", "arrivalTime", "previousStopId", "latTo", "lonTo", "latFrom", "lonFrom"};
            writer.writeNext(headerLine);
            writer.writeAll(dataList);
            writer.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static List<BusStop> optimizeBusRoute(List<BusStop> busStopList, String time, Connection connection){
        List<BusStop> transfers = new ArrayList<>();

        try {
            if(busStopList.size() >= 2){
                int lastIndex = busStopList.size() - 1;

                //fixes issue where first bus stop in a given route does not match the next bus
                String sqlStatement = "SELECT * FROM stop_times WHERE trip_id = ? AND stop_id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement);
                preparedStatement.setString(1,busStopList.get(lastIndex-1).tripId);
                preparedStatement.setString(2, busStopList.get(lastIndex).stopCodeId);
                ResultSet resultSet = preparedStatement.executeQuery();
                if(resultSet.next()){
                    BusStop busStop = convertToBusStopNode(resultSet, connection);
                    if(isTimeOneEarlier(time, busStop.arrivalTime)){
                        busStopList.set(lastIndex,busStop);
                    }
                }
            }

            // sets start and end stops for each route
            for(BusStop busStop : busStopList){
                if(transfers.isEmpty()){
                    transfers.add(busStop);
                }
                else{
                    String prevRouteId = transfers.getLast().routeId;
                    String curRouteId = busStop.routeId;
                    if(!(prevRouteId.equals(curRouteId))){
                        //entering new route, set end time for the previous route

                        String sqlStatement = "SELECT * FROM stop_times WHERE trip_id = ? AND stop_id = ?";
                        PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement);
                        preparedStatement.setString(1, transfers.getLast().tripId);
                        preparedStatement.setString(2, busStop.stopCodeId);
                        ResultSet resultSet = preparedStatement.executeQuery();
                        if(resultSet.next()){
                            BusStop endBusStop = convertToBusStopNode(resultSet,connection);
                            transfers.add(endBusStop);
                        }
                    }
                    transfers.add(busStop);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return transfers;
    }

    public static List<BusStopWeb> convertForWeb(List<BusStop> busStopList, Connection connection){
        List<BusStopWeb> busStopWebList = new ArrayList<>();
        try {
            for(BusStop busStop : busStopList){
                String sqlStatement = "SELECT stop_lat,stop_lon FROM stops WHERE stop_id = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement);
                preparedStatement.setString(1,busStop.stopCodeId);
                ResultSet resultSet = preparedStatement.executeQuery();
                double lat = 0;
                double lon = 0;
                if(resultSet.next()){
                    lat = resultSet.getDouble("stop_lat");
                    lon = resultSet.getDouble("stop_lon");
                }
                busStopWebList.add(new BusStopWeb(busStop, lat, lon));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return busStopWebList;
    }
}