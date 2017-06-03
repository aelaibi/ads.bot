import sys
from subprocess import call

robot_tz = {
    4:'Asia/Riyadh',
    5:'Asia/Riyadh',
    6:'Asia/Riyadh',
    22:'Asia/Riyadh',
    23:'Asia/Riyadh',
    24:'Asia/Riyadh',
    25:'Asia/Riyadh',
    26:'Asia/Riyadh',
    27:'Asia/Riyadh',
    28:'Asia/Riyadh',
    29:'Asia/Riyadh',
    30:'Asia/Riyadh',
    31:'Asia/Riyadh',
    32:'Asia/Riyadh',
    33:'Asia/Riyadh',
    7:'Asia/Kuwait',
    8:'Asia/Kuwait',
    9:'Asia/Kuwait',
    34:'Asia/Kuwait',
    35:'Asia/Kuwait',
    36:'Asia/Kuwait',
    37:'Asia/Kuwait',
    38:'Asia/Kuwait',
    39:'Asia/Kuwait',
    40:'Asia/Kuwait',
    41:'Asia/Kuwait',
    42:'Asia/Kuwait',
    43:'Asia/Kuwait',
    44:'Asia/Kuwait',
    45:'Asia/Kuwait',
    13:'Asia/Beirut',
    14:'Asia/Beirut',
    53:'Asia/Beirut',
    54:'Asia/Beirut',
    55:'Asia/Beirut',
    56:'Asia/Beirut',
    57:'Asia/Beirut',
    58:'Asia/Beirut',
    59:'Asia/Beirut',
    60:'Asia/Beirut',
    10:'Africa/Casablanca',
    11:'Africa/Casablanca',
    12:'Africa/Casablanca',
    46:'Africa/Casablanca',
    47:'Africa/Casablanca',
    48:'Africa/Casablanca',
    49:'Africa/Casablanca',
    50:'Africa/Casablanca',
    51:'Africa/Casablanca',
    52:'Africa/Casablanca',
    66:'Africa/Casablanca',
    67:'Africa/Casablanca',
    68:'Africa/Casablanca',
    69:'Africa/Casablanca',
    70:'Africa/Casablanca',
    1:'Asia/Dubai',
    2:'Asia/Dubai',
    3:'Asia/Dubai',
    15:'Asia/Dubai',
    16:'Asia/Dubai',
    17:'Asia/Dubai',
    18:'Asia/Dubai',
    19:'Asia/Dubai',
    20:'Asia/Dubai',
    21:'Asia/Dubai',
    61:'Asia/Dubai',
    62:'Asia/Dubai',
    63:'Asia/Dubai',
    64:'Asia/Dubai',
    65:'Asia/Dubai',
    71:'Africa/Cairo',
    72:'Africa/Cairo',
    73:'Africa/Cairo',
    74:'Africa/Cairo',
    75:'Africa/Cairo',
    76:'Africa/Cairo',
    77:'Africa/Cairo',
    78:'Africa/Cairo',
    79:'Africa/Cairo',
    80:'Africa/Cairo',
    81:'Africa/Cairo',
    82:'Africa/Cairo',
    83:'Africa/Cairo',
    84:'Africa/Cairo',
    85:'Africa/Cairo',
    86:'Asia/Amman',
    87:'Asia/Amman',
    88:'Asia/Amman',
    89:'Asia/Amman',
    90:'Asia/Amman',
    91:'Asia/Amman',
    92:'Asia/Amman',
    93:'Asia/Amman',
    94:'Asia/Amman',
    95:'Asia/Amman',
    96:'Asia/Amman',
    97:'Asia/Amman',
    98:'Asia/Amman',
    99:'Asia/Amman',
    100:'Asia/Amman',
    101:'Asia/Riyadh',
    102:'Asia/Riyadh',
    103:'Asia/Riyadh',
    104:'Asia/Riyadh',
    105:'Asia/Riyadh',
    106:'Asia/Riyadh',
    107:'Asia/Riyadh',
    108:'Asia/Riyadh',
    109:'Asia/Riyadh',
    110:'Asia/Riyadh',
    111:'Asia/Riyadh',
    112:'Asia/Riyadh',
    113:'Asia/Riyadh',
    114:'Asia/Riyadh',
    115:'Asia/Riyadh',
    116:'Asia/Dubai',
    117:'Asia/Dubai',
    118:'Asia/Dubai',
    119:'Asia/Dubai',
    120:'Asia/Dubai',
    121:'Asia/Dubai',
    122:'Asia/Dubai',
    123:'Asia/Dubai',
    124:'Asia/Dubai',
    125:'Asia/Dubai',
    126:'Asia/Dubai',
    127:'Asia/Dubai',
    128:'Asia/Dubai',
    129:'Asia/Dubai',
    130:'Asia/Dubai',
    131: 'Asia/Amman',
    132: 'Asia/Amman',
    133: 'Asia/Amman',
    134: 'Asia/Amman',
    135: 'Asia/Amman',
    136: 'Asia/Amman',
    137: 'Asia/Amman',
    138: 'Asia/Amman',
    139: 'Asia/Amman',
    140: 'Asia/Amman',
    141: 'Asia/Amman',
    142: 'Asia/Amman',
    143: 'Asia/Amman',
    144: 'Asia/Amman',
    145: 'Asia/Amman',
}

docker_hosts = {
    'c' : '1h1' ,
    's0' : '1h2',
    's' : '1h3',
    'j' : '1h11',
    's2' : '1h10',
    'j2' : '1h9',
    'b' : '1h5',
    'h' : '1h4',
    'jo' : '1h7',
    'e' : '1h8'
}

robots_country ={
    'c'  : [10, 11, 12, 46, 47, 48, 49, 50, 51, 52, 66, 67, 68, 69, 70] 	#MA
    ,'j' : [4, 5, 6, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115] 		#KSA
    ,'s' : [1, 2, 3, 15, 16, 17, 18, 19, 20, 21, 61, 62, 63, 64, 65, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 127, 128, 129, 130] 		#UAE
    ,'h' : [7, 8, 9, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45] 		#KWT
    ,'b' : [13, 14, 53, 54, 55, 56, 57, 58, 59, 60] 						#LEB
    ,'e' : [71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85] 	#EGY
    ,'jo' : [86 ,87 ,88 ,89 ,90 ,91 ,92 ,93 ,94 ,95 ,96 ,97 ,98 ,99, 100, 131,132,133,134,135,136,137,138,139,140,141,142,143,144,145] 	#JOR
}

def createRobot(h=0,id=0):
    #call("rancher hosts", shell=True)
    port_ = '99'
    createCmd = "rancher --host {host} docker run  --restart=always --name robot-{rId} -e ROBOT_BIN=ads.robot-1.0-RELEASE -e ROBOT_ID={rId} -e TZ={timeZone} -e ARTIFACTS_HOST={artifact} -d -p {port}{rId}:5901 aelaibi/robotu:v1.6"
    removeCmd = "rancher --host {host} docker rm -f robot-{rId} "

    robot_link ="http://monitoring.pixitrend.com:85"
    dockerhost = h #docker_hosts[h]
    tz = robot_tz[int(id)]

    if int(id)>99 :
        port_ ='9'

    print removeCmd.format(host=dockerhost,rId=id)
    call(removeCmd.format(host=dockerhost,rId=id), shell=True)

    print createCmd.format(host=dockerhost,rId=id,timeZone=tz,artifact=robot_link, port=port_)
    call(createCmd.format(host=dockerhost,rId=id,timeZone=tz,artifact=robot_link, port=port_), shell=True)


def createAll():
    for c in robots_country:
        print('-----')
        #print(c)
        for rID in robots_country[c]:
            #print(rID)
            createRobot(h=c,id=rID)


def create(host='toto', id=0):
    print "hello from test"
    print host
    print id

if __name__ == '__main__':
    host = sys.argv[1]
    ids = sys.argv[2].replace(" ","")
    if ids == 'ALL':
        #createAll()
        print "please create this method first"
    else:
        for id in ids.split(','):
            createRobot(host,id)