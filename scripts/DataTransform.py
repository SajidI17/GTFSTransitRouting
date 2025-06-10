import os
#TODO: add check for output filepath

def AddZero(inputPath, files, outputPath):
    try:
        for file in files:
            inputFullPath = inputPath + file
            outputFullPath = outputPath + file
            print("Processing " + inputFullPath)
            inFile = open(inputFullPath, 'r', encoding='utf-8')
            outFile = open(outputFullPath, 'w', encoding='utf-8')
            
            for num, line in enumerate(inFile):
                if(num == 0):
                    continue

                stripLine = line.rstrip()
                newLine = stripLine
                
                if (stripLine.endswith(',')):
                    newLine = stripLine + '0'
                
                outFile.write(newLine + '\n')
            
            inFile.close()
            outFile.close()
            print("Process finished for " + outputFullPath)

    except Exception as e:
        print(e)
    

if __name__ == "__main__":
    absolutePath = os.path.dirname(__file__)
    fileListOne = ["routes.txt", "stops.txt", "trips.txt", "stop_times.txt", "calendar.txt", "calendar_dates"]
    iFile = os.path.join(absolutePath, '..\\GTFS dump\\')
    oFile = os.path.join(absolutePath, '..\\GTFS dump\\Output\\')
    AddZero(iFile, fileListOne, oFile)