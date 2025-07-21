import plotly.express as px
import pandas as pd
import os
import plotly.graph_objects as go
import random

DATABASENAME = "OCGTFS"
USER = "postgres"
PASSWORD = "admin"
HOST = "localhost"
PORT = 5432
IMPORTFILEPATH = os.path.join(os.path.dirname(__file__), '..\\')

#######################################################################################################
# CreateCSVFile() no longer needed, Java application automatically creates the csv file needed
#######################################################################################################

# columns = ['stopCodeId', 'tripId', 'routeId', 'arrivalTime', 'previousStopId', 'latTo', 'lonTo', 'latFrom', 'lonFrom']

# def ConvertTextToArray():
#     resultArr = []
#     for line in data:
#             temp = []
#             for i in range(5):
#                 firstIndex = line.find("\'")
#                 secondIndex = line.find("\'",firstIndex+1)
#                 temp.append(line[firstIndex+1:secondIndex])
#                 line = line[secondIndex+1:]
#             resultArr.append(temp)
#     return resultArr

# def CreateCSVFile():
#     connection = psycopg2.connect(database=DATABASENAME, user=USER, password=PASSWORD, host=HOST, port=PORT)
#     cursor = connection.cursor()
    
#     with open((IMPORTFILEPATH + "stopLocations.csv"), 'w', newline='') as csvFile:
#         writer = csv.writer(csvFile)
#         writer.writerow(columns)
#         resultArr = ConvertTextToArray()
#         for arr in resultArr:
#             cursor.execute(("SELECT stop_lat,stop_lon FROM stops WHERE stop_id = '" + arr[0] + "'"))
#             result = cursor.fetchall()
#             arr.extend(result[0])

#             cursor.execute(("SELECT stop_lat,stop_lon FROM stops WHERE stop_id = '" + arr[4] + "'"))
#             result = cursor.fetchall()
#             arr.extend(result[0])

#             writer.writerow(arr)


def ShowMap():
    data = pd.read_csv(IMPORTFILEPATH + "stopLocations.csv")
    data.dropna(axis=0, how='any', subset=None, inplace=True)
    
    fig = go.Figure()
    previousRouteId = '0'
    colourValue = 'rgb(0,0,0)'
    for col,row in data.iterrows():
        if(str(row['routeId']) != str(previousRouteId)):    
            r = random.randint(100,255)
            g = random.randint(0,100)
            b = random.randint(0,100)
            colourValue = 'rgb(' + str(r) + ',' + str(g) + ',' + str(b) + ')'
            previousRouteId = str(row['routeId'])

        nameString = "routeId = " + str(row['routeId']) + ", arrivalTime = " + str(row['arrivalTime'])


        fig.add_trace(go.Scattermapbox(
              mode='lines',
              lon=[row['lonFrom'], row['lonTo']],
              lat=[row['latFrom'], row['latTo']],
              line_color=colourValue,
              name=nameString,
              showlegend=True,
              line=dict(width=5)
        ))

    fig.update_layout(
         mapbox=dict(
              zoom=12,
              center=dict(lon=-75.7003, lat=45.4201)
         )
    )
    fig.update_layout(hoverlabel=dict(namelength=-1))
    fig.update_layout(mapbox_style="open-street-map")
    fig.update_layout(margin={"r":0,"t":0,"l":0,"b":0})
    config = {'scrollZoom': True}
    fig.show(config=config)

if __name__ == "__main__":
    ShowMap()



