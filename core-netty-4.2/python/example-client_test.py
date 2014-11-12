import comm_pb2
import socket
import time
import struct
import base64


def buildPing(tag, number):

    r = comm_pb2.Request()

    r.body.ping.tag = str(tag)
    r.body.ping.number = number


    r.header.originator = 0
    r.header.tag = str(tag + number + int(round(time.time() * 1000)))
    r.header.routing_id = comm_pb2.Header.PING
    r.header.toNode = 1

    msg = r.SerializeToString()
    return msg

def buildJob(name_space, jobAction, ownerId):

    jobId = str(int(round(time.time() * 1000)))

    r = comm_pb2.Request()

    r.body.job_op.job_id = jobId
    r.body.job_op.action = jobAction

    r.body.job_op.data.name_space = name_space
    r.body.job_op.data.owner_id = ownerId
    r.body.job_op.data.job_id = jobId
    r.body.job_op.data.status = comm_pb2.JobDesc.JOBQUEUED

    r.body.job_op.data.options.node_type = comm_pb2.NameValueSet.NODE
    r.body.job_op.data.options.name = "email"
    r.body.job_op.data.options.value = "shajit4"

    nvs = comm_pb2.NameValueSet()
    nvs.node_type = comm_pb2.NameValueSet.NODE
    nvs.name = "test"
    nvs.value = "success"
    r.body.job_op.data.options.node.extend([nvs])

    r.header.originator = 0
    r.header.routing_id = comm_pb2.Header.JOBS
    r.header.toNode = 1

    msg = r.SerializeToString()
    return msg

def buildSaveImageJob(iname, data, ownerId):
    jobId = str(int(round(time.time() * 1000)))
    r= comm_pb2.Request()

    r.header.photoHeader.requestType = 1
    r.body.photoPayload.name=iname
    r.body.photoPayload.data = data



##    r.body.job_op.data.options.node.extend([iname, data])

    r.header.originator = 0
    r.header.routing_id = comm_pb2.Header.JOBS
    r.header.toNode = 1
    msg = r.SerializeToString()
    return msg


def buildSignupJob(username, password,firstName, lastName, ownerId):

    jobId = str(int(round(time.time() * 1000)))

    r = comm_pb2.Request()

    r.body.job_op.job_id = jobId
    r.body.job_op.action = comm_pb2.JobOperation.ADDJOB

    r.body.job_op.data.name_space = "sign_up"
    r.body.job_op.data.owner_id = ownerId
    r.body.job_op.data.job_id = jobId
    r.body.job_op.data.status = comm_pb2.JobDesc.JOBQUEUED

    r.body.job_op.data.options.node_type = comm_pb2.NameValueSet.NODE
    r.body.job_op.data.options.name = "operation"
    r.body.job_op.data.options.value = "sign_up"

    email = comm_pb2.NameValueSet()
    email.node_type = comm_pb2.NameValueSet.NODE
    email.name = "email"
    email.value = username

    psw = comm_pb2.NameValueSet()
    psw.node_type = comm_pb2.NameValueSet.NODE
    psw.name = "password"
    psw.value = password

    fName = comm_pb2.NameValueSet()
    fName.node_type = comm_pb2.NameValueSet.NODE
    fName.name = "firstName"
    fName.value = firstName

    lName = comm_pb2.NameValueSet()
    lName.node_type = comm_pb2.NameValueSet.NODE
    lName.name = "lastName"
    lName.value = lastName

    r.body.job_op.data.options.node.extend([email, psw, fName, lName])

    r.header.originator = 0
    r.header.routing_id = comm_pb2.Header.JOBS
    r.header.toNode = 1

    msg = r.SerializeToString()
    return msg

def buildSigninJob(username, password,ownerId):

    jobId = str(int(round(time.time() * 1000)))

    r = comm_pb2.Request()

    r.body.job_op.job_id = jobId
    r.body.job_op.action = comm_pb2.JobOperation.ADDJOB

    r.body.job_op.data.name_space = "sign_in"
    r.body.job_op.data.owner_id = ownerId
    r.body.job_op.data.job_id = jobId
    r.body.job_op.data.status = comm_pb2.JobDesc.JOBQUEUED

    r.body.job_op.data.options.node_type = comm_pb2.NameValueSet.NODE
    r.body.job_op.data.options.name = "operation"
    r.body.job_op.data.options.value = "sign_in"

    email = comm_pb2.NameValueSet()
    email.node_type = comm_pb2.NameValueSet.NODE
    email.name = "email"
    email.value = username

    psw = comm_pb2.NameValueSet()
    psw.node_type = comm_pb2.NameValueSet.NODE
    psw.name = "password"
    psw.value = password

    r.body.job_op.data.options.node.extend([email, psw])

    r.header.originator = 0
    r.header.routing_id = comm_pb2.Header.JOBS
    r.header.toNode = 1

    msg = r.SerializeToString()
    return msg

def buildCourseDescJob(courseName, ownerId):

    jobId = str(int(round(time.time() * 1000)))

    r = comm_pb2.Request()

    r.body.job_op.job_id = jobId
    r.body.job_op.action = comm_pb2.JobOperation.ADDJOB

    r.body.job_op.data.name_space = "getdescription"
    r.body.job_op.data.owner_id = ownerId
    r.body.job_op.data.job_id = jobId
    r.body.job_op.data.status = comm_pb2.JobDesc.JOBQUEUED

    r.body.job_op.data.options.node_type = comm_pb2.NameValueSet.VALUE
    r.body.job_op.data.options.name = "coursename"
    r.body.job_op.data.options.value = courseName

    r.header.originator = 0
    r.header.routing_id = comm_pb2.Header.JOBS
    r.header.toNode = 1

    msg = r.SerializeToString()
    return msg
def buildNS():
    r = comm_pb2.Request()

    r.body.space_op.action = comm_pb2.NameSpaceOperation.ADDSPACE


    r.header.originator = 0
    r.header.tag = str(int(round(time.time() * 1000)))
    r.header.routing_id = comm_pb2.Header.NAMESPACES

    m = r.SerializeToString()
    return m

def buildListCourse(ownerId):

    jobId = str(int(round(time.time() * 1000)))

    r = comm_pb2.Request()

    r.body.job_op.job_id = jobId
    r.body.job_op.action = comm_pb2.JobOperation.ADDJOB

    r.body.job_op.data.name_space = "listcourses"
    r.body.job_op.data.owner_id = ownerId
    r.body.job_op.data.job_id = jobId
    r.body.job_op.data.status = comm_pb2.JobDesc.JOBQUEUED

    r.body.job_op.data.options.node_type = comm_pb2.NameValueSet.NODE
    r.body.job_op.data.options.name = "operation"
    r.body.job_op.data.options.value = "listcourses"

    uId = comm_pb2.NameValueSet()
    uId.node_type = comm_pb2.NameValueSet.NODE
    uId.name = "uId"
    uId.value = "12"

    r.body.job_op.data.options.node.extend([uId])

    r.header.originator = "python client"
    r.header.routing_id = comm_pb2.Header.JOBS
    r.header.toNode = 1

    msg = r.SerializeToString()
    return msg

def buildQuestionJob(myTitle, myDescription, myPostdate, ownerId):

    jobId = str(int(round(time.time() * 1000)))
    r = comm_pb2.Request()

    r.body.job_op.job_id = jobId
    r.body.job_op.action = comm_pb2.JobOperation.ADDJOB

    r.body.job_op.data.name_space = "questionadd"
    r.body.job_op.data.owner_id = ownerId
    r.body.job_op.data.job_id = jobId
    r.body.job_op.data.status = comm_pb2.JobDesc.JOBQUEUED

    r.body.job_op.data.options.node_type = comm_pb2.NameValueSet.NODE
    r.body.job_op.data.options.name = "operation"
    r.body.job_op.data.options.value = "QuestionJob"

    title = comm_pb2.NameValueSet()
    title.node_type = comm_pb2.NameValueSet.NODE
    title.name = "title"
    title.value = myTitle

    me = comm_pb2.NameValueSet()
    me.node_type = comm_pb2.NameValueSet.NODE
    me.name = "owner"
    me.value = str(ownerId)

    description = comm_pb2.NameValueSet()
    description.node_type = comm_pb2.NameValueSet.NODE
    description.name = "description"
    description.value = myDescription

    postdate = comm_pb2.NameValueSet()
    postdate.node_type = comm_pb2.NameValueSet.NODE
    postdate.name = "postdate"
    postdate.value = myPostdate

    r.body.job_op.data.options.node.extend([title, me, description, postdate])
    r.header.originator = 0
    r.header.routing_id = comm_pb2.Header.JOBS
    r.header.toNode = 1

    msg = r.SerializeToString()
    return msg

def buildAnswerJob(qId, myDescription, ownerId):

    jobId = str(int(round(time.time() * 1000)))
    r = comm_pb2.Request()

    r.body.job_op.job_id = jobId
    r.body.job_op.action = comm_pb2.JobOperation.ADDJOB

    r.body.job_op.data.name_space = "answeradd"
    r.body.job_op.data.owner_id = ownerId
    r.body.job_op.data.job_id = jobId
    r.body.job_op.data.status = comm_pb2.JobDesc.JOBQUEUED

    r.body.job_op.data.options.node_type = comm_pb2.NameValueSet.NODE
    r.body.job_op.data.options.name = "operation"
    r.body.job_op.data.options.value = "AnswerJob"

    question = comm_pb2.NameValueSet()
    question.node_type = comm_pb2.NameValueSet.NODE
    question.name = "qId"
    question.value = qId

    me = comm_pb2.NameValueSet()
    me.node_type = comm_pb2.NameValueSet.NODE
    me.name = "uId"
    me.value = str(ownerId)

    description = comm_pb2.NameValueSet()
    description.node_type = comm_pb2.NameValueSet.NODE
    description.name = "description"
    description.value = myDescription

    r.body.job_op.data.options.node.extend([question, me, description])
    r.header.originator = "python client"
    r.header.routing_id = comm_pb2.Header.JOBS
    r.header.toNode = str(0)

    msg = r.SerializeToString()
    return msg

def buildShowQuestionsJob(ownerId):

    jobId = str(int(round(time.time() * 1000)))
    r = comm_pb2.Request()

    r.body.job_op.job_id = jobId
    r.body.job_op.action = comm_pb2.JobOperation.ADDJOB

    r.body.job_op.data.name_space = "questionshow"
    r.body.job_op.data.owner_id = ownerId
    r.body.job_op.data.job_id = jobId
    r.body.job_op.data.status = comm_pb2.JobDesc.JOBQUEUED

    r.body.job_op.data.options.node_type = comm_pb2.NameValueSet.NODE
    r.body.job_op.data.options.name = "operation"
    r.body.job_op.data.options.value = "QuestionJob"

    r.header.originator = "python client"
    r.header.routing_id = comm_pb2.Header.JOBS
    r.header.toNode = str(0)

    msg = r.SerializeToString()
    return msg

def buildShowAnswersJob(ownerId):

    jobId = str(int(round(time.time() * 1000)))
    r = comm_pb2.Request()

    r.body.job_op.job_id = jobId
    r.body.job_op.action = comm_pb2.JobOperation.ADDJOB

    r.body.job_op.data.name_space = "answershow"
    r.body.job_op.data.owner_id = ownerId
    r.body.job_op.data.job_id = jobId
    r.body.job_op.data.status = comm_pb2.JobDesc.JOBQUEUED

    r.body.job_op.data.options.node_type = comm_pb2.NameValueSet.NODE
    r.body.job_op.data.options.name = "operation"
    r.body.job_op.data.options.value = "ShowAnswerJob"

    r.header.originator = "python client"
    r.header.routing_id = comm_pb2.Header.JOBS
    r.header.toNode = str(0)

    msg = r.SerializeToString()
    return msg

def buildCompetitionJob(name_space, jobAction, ownerId):

    jobId = str(int(round(time.time() * 1000)))

    r = comm_pb2.Request()

    r.body.job_op.job_id = jobId
    r.body.job_op.action = comm_pb2.JobOperation.ADDJOB

    r.body.job_op.data.name_space = name_space
    r.body.job_op.data.owner_id = ownerId
    r.body.job_op.data.job_id = jobId
    r.body.job_op.data.status = comm_pb2.JobDesc.JOBQUEUED


    r.header.originator = 0
    r.header.routing_id = comm_pb2.Header.JOBS
    r.header.toNode = 1

    msg = r.SerializeToString()
    return msg


def sendMsg(msg_out, port, host):
    s = socket.socket()
#    host = socket.gethostname()
#    host = "192.168.0.87"

    s.connect((host, port))
    msg_len = struct.pack('>L', len(msg_out))
    s.sendall(msg_len + msg_out)
    len_buf = receiveMsg(s, 4)
    msg_in_len = struct.unpack('>L', len_buf)[0]
    msg_in = receiveMsg(s, msg_in_len)

    r = comm_pb2.Request()
    r.ParseFromString(msg_in)
#    print msg_in
#    print r.body.job_status
#    print r.header.reply_msg
#    print r.body.job_op.data.options
    s.close
    return r
def receiveMsg(socket, n):
    buf = ''
    while n > 0:
        data = socket.recv(n)
        if data == '':
            raise RuntimeError('data not received!')
        buf += data
        n -= len(data)
    return buf


def getBroadcastMsg(port):
    # listen for the broadcast from the leader"

    sock = socket.socket(socket.AF_INET,  # Internet
                        socket.SOCK_DGRAM)  # UDP

    sock.bind(('', port))

    data = sock.recv(1024)  # buffer size is 1024 bytes
    return data


if __name__ == '__main__':
    # msg = buildPing(1, 2)
    # UDP_PORT = 8080
    # serverPort = getBroadcastMsg(UDP_PORT)

    host = "localhost" #raw_input("IP:")
    port = 5571 #raw_input("Port:")

    port = int(port)
    whoAmI = 1;
   


    fh = open('index.jpeg','rb')
    dataFile = fh.read()
    iname="picture"
    ##print data
    fh.close()
    data = base64.b64encode(dataFile)
    saveimageJob = buildSaveImageJob(iname, data, 1)
    result = sendMsg(saveimageJob, port, host)
    print result.body.photoPayload.uuid 

   
