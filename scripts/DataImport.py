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
        cursor.copy_from(f, file, sep=",")
        connection.commit()
        print("Finish import for table: " + file)

def BusStopDistances():
    busstops = []
    connection = psycopg2.connect(database=DATABASENAME, user=USER, password=PASSWORD, host=HOST, port=PORT)
    cursor = connection.cursor()
    cursor.execute("SELECT * FROM stops")
    result = cursor.fetchall()
    for i in range(len(result)):
        for j in range(i + 1, len(result)):
            distance = HaversineDistance(result[i][5], result[i][6], result[j][5], result[j][6])
            if(distance <= 0.5):
                busstops.append((result[i][2],result[j][2]))
    return busstops


# Haversine Formula derived from Wikipedia https://en.wikipedia.org/wiki/Haversine_formula
def HaversineDistance(lat1, lon1, lat2, lon2):
    r = 6371
    p = math.pi / 180
    a = (1 - math.cos((lat2 - lat1) * p) + math.cos(lat1 * p) * math.cos(lat2 * p) * (1 - math.cos((lon2 - lon1) * p)))/2
    distance = 2 * r * math.asin(math.sqrt(a))
    return distance

    


if __name__ == "__main__":
    #fileListOne = ["routes", "stops", "trips", "stop_times", "calendar"]
    fileListOne = ["stops"]
    ImportDataIntoDatabase(fileListOne)
    print("Done!")