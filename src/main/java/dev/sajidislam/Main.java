package dev.sajidislam;
import java.sql.*;
import java.util.*;

// Monday - 0, Sunday - 7

public class Main {
    private static final String URL = "jdbc:postgresql://localhost:5432/OCGTFS"; //Change OCGTFS to the name of the database you have
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "admin";
    private static Map<String, ServiceType> serviceTypeMap;

    public static void main(String[] args) {
        serviceTypeMap = new HashMap<>();
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
    public static List<BusRecord> getAllDepartingBusses(String busStopId, String time, Connection connection){
        List<BusRecord> BusRecordList = new ArrayList<>();
        try{

            //get all buses leaving a particular stop within 20 minutes of time
            String sqlStatement = "SELECT * FROM stop_times WHERE stop_Id = ? AND arrival_time > (?::interval) AND arrival_time <= (?::interval + INTERVAL '25 minutes')";
            PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement);
            preparedStatement.setString(1,busStopId);
            preparedStatement.setObject(2, time);
            preparedStatement.setObject(3, time);
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

    public static void visitingBusStops(List<BusRecord> busRecordList, Connection connection){
        Graph<BusStop> busGraph = new Graph<>();

        try {
            for (BusRecord busRecord : busRecordList){
                String sqlStatement = "SELECT * FROM stop_times WHERE trip_id = ? AND stop_sequence >= ?";
                PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement);
                preparedStatement.setString(1,busRecord.tripId);
                preparedStatement.setInt(2,busRecord.stopSequence);

                BusStop prevStop = null;
                ResultSet resultSet = preparedStatement.executeQuery();


                if (resultSet.next()){
                    String stopId = resultSet.getString("stop_id");
                    String tripId = resultSet.getString("trip_id");
                    String routeId = getRouteIdFromTripId(tripId, connection);
                    String arrivalTime = resultSet.getObject("arrival_time").toString();
                    prevStop = new BusStop(stopId,tripId,routeId,arrivalTime);
                }

                while(resultSet.next()){
                    String stopId = resultSet.getString("stop_id");
                    String tripId = resultSet.getString("trip_id");
                    String routeId = getRouteIdFromTripId(tripId, connection);
                    String arrivalTime = resultSet.getObject("arrival_time").toString();

                    BusStop curStop = new BusStop(stopId,tripId,routeId,arrivalTime);

                    //todo: add math for string arrival times
                    //todo: add logic for when an edge should form
                    busGraph.addEdge(prevStop, curStop, 1.0F);

                    prevStop = curStop;

                }
            }


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /// Generates the graph that will be used by Dijkstra's algorithm
    public static void createTopologicalGraph(int busStopOrigin, String time, int date, Connection connection){
        try{
            String busStopOriginId = convertCodeToId(busStopOrigin, connection);
            List<BusRecord> busArrivals = getAllDepartingBusses(busStopOriginId, time, connection);
            for (BusRecord busArrival : busArrivals) {
                System.out.println(busArrival.toString());
            }
            visitingBusStops(busArrivals,connection);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}