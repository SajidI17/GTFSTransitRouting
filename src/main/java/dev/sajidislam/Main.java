package dev.sajidislam;
import java.sql.*;
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

    public static void main(String[] args) {
        serviceTypeMap = new HashMap<>();
        busNetwork = new Graph();
        try{
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            int date = 20250603;
            setSchedules(connection, date);
            createTopologicalGraph(7851, "09:00:00", date, connection);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
    public static List<BusRecord> getAllDepartingBusses(String busStopId, String time, String excludeTripId, Connection connection){
        List<BusRecord> BusRecordList = new ArrayList<>();
        try{

            //get all buses leaving a particular stop within 20 minutes of time
            String sqlStatement = "SELECT * FROM stop_times WHERE stop_Id = ? AND arrival_time > (?::interval) AND arrival_time <= (?::interval + INTERVAL '25 minutes') AND trip_id != ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement);
            preparedStatement.setString(1,busStopId);
            preparedStatement.setObject(2, time);
            preparedStatement.setObject(3, time);
            preparedStatement.setString(4,excludeTripId);
            ResultSet resultSet = preparedStatement.executeQuery();

            //check that these buses are running on that particular day
            while (resultSet.next()) {
                String tripId = resultSet.getString("trip_id");
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

    public static List<BusStop> visitingBusStops(List<BusRecord> busRecordList, Connection connection){
        List<BusStop> busStopsAdded = new ArrayList<>();
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

                    //debug
                    if(curStop.stopCodeId.equals("CB990")){
                        System.out.println("==========================STOP FOUND==========================");
                    }

                    assert prevStop != null;
                    //if the curStop does not exist, then can create an edge to curStop
                    //as curStop did not exist in graph, it did not have any incoming edges, thus edge can be added without issue
                    if(!busNetwork.doesNodeExist(curStop.stopCodeId)){
                        //set what the previous stop is
                        curStop.previousStopId = (prevStop.stopCodeId);

                        //add edge
                        busNetwork.addEdge(prevStop,curStop,stringTimeDifferences(prevStop.arrivalTime, curStop.arrivalTime));

                        //add to list noting new stop was added to graph
                        busStopsAdded.add(curStop);

                        //make cur the previous stop
                        prevStop = curStop;
                    }
                    else{
                        //curStop already exists in graph

                        //get the current nodes saved in the graph
                        BusStop currentStop = busNetwork.getBusStop(curStop.stopCodeId);

                        //todo: this is a temporarily fix, accounting for first BusStop which would have no previousStopId set
                        if(currentStop.previousStopId == null){
                            continue;
                        }

                        BusStop previousStop = busNetwork.getBusStop(currentStop.previousStopId);

                        boolean isEarlier = isTimeOneEarlier(curStop.arrivalTime, currentStop.arrivalTime);
                        if(isEarlier){
                            //as the stop is earlier, we need to adjust edges

                            // remove the old edge that was connecting to the current stop
                            busNetwork.removeEdge(previousStop.stopCodeId,currentStop.stopCodeId);

                            //update the current bus stop with the new information
                            busNetwork.allBusStops.put(currentStop.stopCodeId, curStop);

                            //create new edge to current bus stop
                            busNetwork.addEdge(prevStop,curStop,stringTimeDifferences(prevStop.arrivalTime, curStop.arrivalTime));

                            //add to list noting new stop was added to graph
                            busStopsAdded.add(curStop);

                            //make cur the previous stop
                            prevStop = curStop;

                            //todo: add queue of bus stops that need to be recalculated
                        }

                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return busStopsAdded;
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

    /// Generates the graph that will be used by Dijkstra's algorithm
    public static void createTopologicalGraph(int busStopOrigin, String time, int date, Connection connection){
        try{
            //convert stopId to something usable
            String busStopOriginId = convertCodeToId(busStopOrigin, connection);

            //get all buses leaving within 20 minutes of time (each record is a single bus leaving that particular stop)
            List<BusRecord> busArrivals = getAllDepartingBusses(busStopOriginId, time, "", connection);

            //debug
            for (BusRecord busArrival : busArrivals) {
                System.out.println(busArrival.toString());
            }

            //get the bus stops the bus is visiting and add to the graph
            List<BusStop> busStopList = visitingBusStops(busArrivals, connection);

            //Since we only want to find routes with at most 3 connections, we loop 2 more times
            for(int i = 0;i < 2; i++){
                List<BusStop> tempBusList = new ArrayList<>();
                for (BusStop busStop : busStopList){
                    List<BusRecord> tempBusArrivals = getAllDepartingBusses(busStop.stopCodeId, time, busStop.tripId, connection);
                    tempBusList = visitingBusStops(tempBusArrivals, connection);
                }
                busStopList = tempBusList;
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}