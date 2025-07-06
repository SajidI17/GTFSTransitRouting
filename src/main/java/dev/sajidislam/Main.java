package dev.sajidislam;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.*;

// Monday - 0, Sunday - 7
//todo: convert string time to java LocalTime

public class Main {
    private static final String URL = "jdbc:postgresql://localhost:5432/OCGTFS"; //Change OCGTFS to the name of the database you have
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "admin";
    private static Map<String, ServiceType> serviceTypeMap;
    private static Graph busNetwork;
    private static List<String> excludeTripIds;

    public static void main(String[] args) {
        long startTime = System.nanoTime();
        serviceTypeMap = new HashMap<>();
        busNetwork = new Graph();
        excludeTripIds = new ArrayList<>();
        excludeTripIds.add("");
        try{
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            int date = 20250603;
            setSchedules(connection, date);
            createTopologicalGraph(7851, "09:00:00", date, connection);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        long endTime = System.nanoTime();
        long totalRunTime = (endTime - startTime) / 1000000000;
        System.out.println("\nTOTAL RUNNING TIME OF ALGORITHM: " + totalRunTime);
    }

    /// For basic user input
    public static int getOriginAndDestination(){
        Scanner input = new Scanner(System.in);
        System.out.println("Please enter bus stop code of origin: ");
        int busStopOrigin = input.nextInt();
        System.out.println("Please enter bus stop code of destination: ");
        int busStopDestination = input.nextInt();
        System.out.println("Please enter date: ");
        String weekDay = input.nextLine();
        System.out.println("Please enter the time you will leave from the origin: ");
        String time = input.nextLine();
        return 0;
    }

    /// Sets the static variable serviceTypeMap with the serviceIDs that run on the user requested date
    public static void setSchedules(Connection connection, int date){
        try{

            // get all service_ids for a given date
            String sqlStatement = "SELECT * FROM calendar_dates WHERE date = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement);
            preparedStatement.setInt(1,date);
            ResultSet resultSet = preparedStatement.executeQuery();

            // This may not be needed. But this is getting the weekdays the serviceId is relevant for
            while (resultSet.next()){

                // get weekday of services for that serviceId
                String serviceId = resultSet.getString("service_id");
                sqlStatement = "SELECT * FROM calendar WHERE service_id = ?";
                preparedStatement = connection.prepareStatement(sqlStatement);
                preparedStatement.setString(1, serviceId);
                ResultSet resultSet2 = preparedStatement.executeQuery();

                while (resultSet2.next()){
                    ServiceType serviceType = new ServiceType(
                            resultSet2.getBoolean("monday"),
                            resultSet2.getBoolean("tuesday"),
                            resultSet2.getBoolean("wednesday"),
                            resultSet2.getBoolean("thursday"),
                            resultSet2.getBoolean("friday"),
                            resultSet2.getBoolean("saturday"),
                            resultSet2.getBoolean("sunday"),
                            date
                    );
                    serviceTypeMap.putIfAbsent(resultSet.getString("service_id"),serviceType);
                }


            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /// Prints compatible dates available in the database, mainly used for UI
    public static List<Integer> getAvailableDates(Connection connection){
        List<Integer> dates = new ArrayList<>();

        try{
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
    public static String convertCodeToId(int busStopCode, Connection connection){
        try {
            String sqlStatement = "SELECT stop_id FROM stops WHERE stop_code = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement);
            preparedStatement.setString(1, String.valueOf(busStopCode));
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
            Array sqlArr = connection.createArrayOf("TEXT", excludeTripIds.toArray());

            //get all buses leaving a particular stop within x minutes of time
            String sqlStatement = "SELECT * FROM stop_times WHERE stop_Id = ? AND arrival_time > (?::interval) AND arrival_time <= (?::interval + INTERVAL '"+ minutes + " minutes') AND NOT (trip_id = ANY(?))";
            PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement);
            preparedStatement.setString(1,busStopId);
            preparedStatement.setObject(2, time);
            preparedStatement.setObject(3, time);
            preparedStatement.setArray(4, sqlArr);
            ResultSet resultSet = preparedStatement.executeQuery();

            //check that these buses are running on that particular day
            while (resultSet.next()) {
                String tripId = resultSet.getString("trip_id");

                //add to list to exclude from future queries
                excludeTripIds.add(tripId);

                sqlStatement = "SELECT * FROM trips WHERE trip_id = ?";
                preparedStatement = connection.prepareStatement(sqlStatement);
                preparedStatement.setString(1,tripId);
                ResultSet resultSet2 = preparedStatement.executeQuery();
                while(resultSet2.next()){

                    //previous code
                    //boolean isRunningCorrectWeek = serviceTypeMap.get(resultSet2.getString("service_id")).isWeekday(weekday);

                    //this checks if the serviceId is for the correct date
                    boolean isRunningCorrectWeek = serviceTypeMap.containsKey(resultSet2.getString("service_id"));

                    if(isRunningCorrectWeek){
                        String arrivalTime = resultSet.getTime("arrival_time").toString();
                        String stopId = resultSet.getString("stop_id");
                        String routeId = resultSet2.getString("route_id");
                        int stopSequence = resultSet.getInt("stop_sequence");
                        String headSign = resultSet2.getString("trip_headsign");
                        String serviceId = resultSet2.getString("service_id");
                        BusRecordList.add(new BusRecord(arrivalTime,stopId,tripId,routeId,stopSequence,headSign,serviceId));
                    }
                }
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

                //sets the previous bus stop
                if (resultSet.next()){
                    prevStop = convertToBusStopNode(resultSet, connection);
                }

                while(resultSet.next()){
                    BusStop curStop = convertToBusStopNode(resultSet, connection);
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
                curStop.previousStopId = previousStop.stopCodeId;

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
    public static void createTopologicalGraph(int busStopOrigin, String time, int date, Connection connection){
        try{
            //convert stopId to something usable
            String busStopOriginId = convertCodeToId(busStopOrigin, connection);

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


            //Since we only want to find routes with at most 3 connections, we loop 2 more times
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
            }

            System.out.println("Graph allBusStops size: " + busNetwork.allBusStops.size() + "\nGraph allBusStops size: " + busNetwork.adjacencyList.size());
            if(busNetwork.doesNodeExist("10738")){
                BusStop testStop = busNetwork.getBusStop("10738");
                while(testStop.previousStopId != null){
                    System.out.println(testStop);
                    testStop = busNetwork.getBusStop(testStop.previousStopId);
                }
                System.out.println(testStop);
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}