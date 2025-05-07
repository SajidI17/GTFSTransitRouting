import psycopg2
import os
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


if __name__ == "__main__":
    fileListOne = ["routes", "stops", "trips", "stop_times", "calendar"]
    ImportDataIntoDatabase(fileListOne)
    print("Done!")