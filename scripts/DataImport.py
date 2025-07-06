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
    cursor.close()
    connection.close()

def BusStopDistances():
    print("Starting creation of transfers table")
    busstops = []
    connection = psycopg2.connect(database=DATABASENAME, user=USER, password=PASSWORD, host=HOST, port=PORT)
    cursor = connection.cursor()
    cursor.execute("SELECT * FROM stops WHERE parent_station IS NOT NULL")
    result = cursor.fetchall()
    for i in range(len(result)):
        for j in range(i + 1, len(result)):
            if(result[i][10] != result[j][10]):
                continue
            distance = HaversineDistance(result[i][5], result[i][6], result[j][5], result[j][6])
            if(distance <= 0.5):
                busstops.append((result[i][0],result[j][0]))
                cursor.execute("INSERT INTO transfers VALUES (%s,%s,6)", (result[i][0], result[j][0]))
    connection.commit()
    cursor.close()
    connection.close()
    return busstops


# Haversine Formula derived from Wikipedia https://en.wikipedia.org/wiki/Haversine_formula
# Returns distance in km
def HaversineDistance(lat1, lon1, lat2, lon2):
    r = 6371
    p = math.pi / 180
    a = (1 - math.cos((lat2 - lat1) * p) + math.cos(lat1 * p) * math.cos(lat2 * p) * (1 - math.cos((lon2 - lon1) * p)))/2
    distance = 2 * r * math.asin(math.sqrt(a))
    return distance


def CreateIndex():
    connection = psycopg2.connect(database=DATABASENAME, user=USER, password=PASSWORD, host=HOST, port=PORT)
    cursor = connection.cursor()
    cursor.execute("CREATE INDEX IF NOT EXISTS indx_stop_times_stop_id_arrival_time ON stop_times (stop_id, arrival_time)")
    cursor.execute("CREATE INDEX IF NOT EXISTS indx_stop_times_trip_id ON stop_times (trip_id)")
    connection.commit()
    cursor.close()
    connection.close()


if __name__ == "__main__":
    #fileListOne = ["routes", "stops", "trips", "stop_times", "calendar", "calendar_dates"]
    fileListOne = ["stops"]
    ImportDataIntoDatabase(fileListOne)

    addedStops = BusStopDistances()
    print(str(len(addedStops)) + " records inserted into transfers table")

    print("Creating necessary indexes")
    CreateIndex()

    print("Completed!")