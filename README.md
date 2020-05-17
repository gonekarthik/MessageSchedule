# MessageSchedule
Scheduling Status Code Message
1. Java - 1.8

2. Maven - 3.6.3

3. MySQL - 8.0

Database name : Message

SqlFile location: \src\main\resources\mysql_tables.sql

Scheduling message API 

http://localhost:8080/scheduleMessage

Sample Request: 

{
	"message":"dear test message",
	"dateTime":"2020-05-17T18:10:00",
	"timeZone":"America/Halifax"
}
