import psycopg2
import os
import math
DATABASENAME = "OCGTFS"
USER = "postgres"
PASSWORD = "admin"
HOST = "localhost"
PORT = 5432
IMPORTFILEPATH = os.path.join(os.path.dirname(__file__), '..\\GTFS dump\\Output\\')


def ImportDataIntoDatabase(files):
    connection = psycopg2.connect(database=DATABASENAME, user=USER, password=PASSWORD, host=HOST, port=PORT)
    cursor = connection.cursor()
    for file in files:
        print("Starting import for table: " + file)
        fullPath = IMPORTFILEPATH + file + ".txt"
        f = open(fullPath)
        #cursor.copy_from(f, file, sep=",")
        sqlStatment = "COPY " + file + " FROM STDIN WITH (FORMAT text, DELIMITER ',', NULL '')"
        cursor.copy_expert(sqlStatment, f)
        connection.commit()
        print("Finish import for table: " + file)

def BusStopDistances():
    print("Starting creation of transfers table")
    busstops = []
    connection = psycopg2.connect(database=DATABASENAME, user=USER, password=PASSWORD, host=HOST, port=PORT)
    cursor = connection.cursor()
    cursor.execute("SELECT * FROM stops")
    result = cursor.fetchall()
    for i in range(len(result)):
        for j in range(i + 1, len(result)):
            distance = HaversineDistance(result[i][5], result[i][6], result[j][5], result[j][6])
            if(distance <= 0.15):
                busstops.append((result[i][0],result[j][0]))
                cursor.execute("INSERT INTO transfers VALUES (%s,%s,6)", (result[i][0], result[j][0]))
    connection.commit()
    print(str(len(busstops)) + " records inserted into transfers table")
    return busstops


# Haversine Formula derived from Wikipedia https://en.wikipedia.org/wiki/Haversine_formula
# Returns distance in km
def HaversineDistance(lat1, lon1, lat2, lon2):
    r = 6371
    p = math.pi / 180
    a = (1 - math.cos((lat2 - lat1) * p) + math.cos(lat1 * p) * math.cos(lat2 * p) * (1 - math.cos((lon2 - lon1) * p)))/2
    distance = 2 * r * math.asin(math.sqrt(a))
    return distance


def CreateSuperNodes():
    busStations = ["Blair", "St-Laurent", "Tremblay", "Hurdman","Lees","uOttawa","Rideau","Parliament","Lyon","Pimisi","Bayview","Tunney's Pasture"]
    busStationCodes = ["3027","3025","3024","3023","3022","3021","3009","3052","3051","3010","3060","3011"]

    if(busStations.count != busStationCodes.count):
        raise Exception("CreateSuperNodes arrays are not equal")
    
    for i in range(len(busStations)):
        connection = psycopg2.connect(database=DATABASENAME, user=USER, password=PASSWORD, host=HOST, port=PORT)
        cursor = connection.cursor()
        cursor.execute("UPDATE stops SET stop_id = " + busStations[i].upper() + "NODE WHERE stop_code = " + busStationCodes[i])
    


if __name__ == "__main__":
    #fileListOne = ["routes", "stops", "trips", "stop_times", "calendar", "calendar_dates"]
    fileListOne = ["stops"]
    ImportDataIntoDatabase(fileListOne)

    BusStopDistances()
    print("Done!")