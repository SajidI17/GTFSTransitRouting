# GTFSTransitRouting

## Prerequisites 
Following items should be installed on your machine before starting
- JDK 21
- PostgreSQL (something like pgadmin can be installed)
- Python 

## Setup
All steps in this section should be followed to setup this application on your machine

### Setting up the project repo
Before the application can be run, setup will be required to setup database
1. Download the repo
2. Create a database in postgres and create the tables using [createtables.sql](https://github.com/SajidI17/GTFSTransitRouting/blob/main/scripts/createtables.sql) (inside the scripts folder)
3. Modify the PostgreSQL database name, username and password for [DataImport.py](https://github.com/SajidI17/GTFSTransitRouting/blob/main/scripts/DataImport.py) and [Main.java](https://github.com/SajidI17/GTFSTransitRouting/blob/main/src/main/java/dev/sajidislam/util/Main.java) as needed
4. Download a copy of the [OC Transpo GTFS dataset](https://www.octranspo.com/en/plan-your-trip/travel-tools/developers/)
5. In the root folder of the repo, create a folder named "GTFS dump"
6. Copy the GTFS dataset inside the folder
7. Create a folder named "Output" inside "GTFS dump"

### Preparing the database
1. First run [DataTransform.py](https://github.com/SajidI17/GTFSTransitRouting/blob/main/scripts/DataTransform.py) to convert the files to something usable by PostgreSQL and wait for it to finish
2. Then run [DataImport.py](https://github.com/SajidI17/GTFSTransitRouting/blob/main/scripts/DataImport.py) to copy the data, modify data, and create indexes

### Running the Application
1. Open the pom.xml file as a project using PostgreSQL
2. Run [GtfsTransitRoutingApplication.java](https://github.com/SajidI17/GTFSTransitRouting/blob/main/src/main/java/dev/sajidislam/GtfsTransitRoutingApplication.java) to start the backend server
3. Run [index.html](https://github.com/SajidI17/GTFSTransitRouting/blob/main/website/index.html) inside the websites folder to interact with the application


## Libraries Used

### Python
- psycopg2: For connection to the database with python
- plotly and pandas: For the visualization debugging of the results (internal tool only)

### Java
- SpringBoot: The framework running the backend server
- JDBC PostgreSQL Driver: For connection to the database with Java

### Website
- Leaflet: For creation of the map and visualization of the routing results