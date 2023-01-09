use F1Prediction;

drop table circuits;

Create table if not exists DriverRaceWeekend(
PrimKey int(11) auto_increment primary key,
CarNumber int(2),
DriversID int(11),
StartingPoints int(3),
RaceCalenderID int(11),
PreviousRace3 int(2),
PreviousRace2 int(2),
PreviousRace1 int(2),
FP3Rank int(2),
StartPos int(2),
FinishPos int(2),
foreign key(DriversID) references Drivers(DriverPrimKey) ON DELETE CASCADE,
foreign key(RaceCalenderID) references RaceCalender(RaceCalenderPrimKey) ON DELETE CASCADE
);

Create table if not exists circuits (
CircuitPrimKey int(11) auto_increment primary key,
CircuitName varchar(64) NOT NULL,
FirstYear year(4) NOT NULL,
LastYear year(4) NOT NULL,
Region varchar(30),
Country varchar(30) NOT NULL,
GrandsPrixHeld int(3) NOT NULL,
Length float(5) NOT NULL,
Turns int(3) NOT NULL,
LapRecord varchar(9),
LapRecordHolder varchar(50),
Description varchar(700) NOT NULL,
ImageLink varchar(255)
);

Create table if not exists Drivers(
DriverPrimKey int(11) auto_increment primary key,
FirstName varchar(30) not null,
MiddleName varchar(50),
LastName varchar(50) not null,
AbbreviatedName varchar(40) not null,
DateofBirth year,
HomeCountry varchar(30),
Region varchar(50),
Height int(3),
Weight int(3),
SeasonWins int(2),
RaceStarts int(3),
RaceWins int(3),
Podiums int(3),
PolePositions int(3),
LapsRaced int(5),
LapsLed int(4),
ImageLink varchar(255)
);

Create table if not exists RaceCalender(
RaceCalenderPrimKey int(11) auto_increment primary key,
CircuitID int(11),
Season int(4),
RaceDate date,
RaceTitle varchar(60),
GrandPrix varchar(30),
foreign key(CircuitID) references circuits(CircuitPrimKey) ON DELETE CASCADE
);

Create table if not exists PredictionsLeague(
PredictionsLeaguePrimKey int(11) auto_increment primary key,
UserEamil varchar(256),
NickName varchar(256),
LeaguePoints int(5)
);

Create table if not exists Predictions(
PredictionsPrimKey int(11) auto_increment primary key,
RaceCalenderID int(11),
PredictionLeagueID int(11),
FirstPos int(11),
SecondPos int(11),
ThirdPos int(11),
FourthPos int(11),
FifthPos int(11),
SixthPos int(11),
SeventhPos int(11),
EightPos int(11),
NinethPos int(11),
TenthPos int(11),
EleventhPos int(11),
TwelftPos int(11),
ThirteenthPos int(11),
FourteenthPos int(11),
FifteenthPos int(11),
SixteenthPos int(11),
SeventeenthPos int(11),
EighteenthPos int(11),
NineteenthPos int(11),
TwentiethPos int(11),
TwentiethFirstPos int(11),
TwentiethSecondPos int(11),
foreign key(RaceCalenderID) references RaceCalender(RaceCalenderPrimKey) ON DELETE CASCADE,
foreign key(PredictionLeagueID) references PredictionsLeague(PredictionsLeaguePrimKey) ON DELETE CASCADE,
foreign key(FirstPos) references Drivers(DriverPrimKey) ON DELETE CASCADE,
foreign key(SecondPos) references Drivers(DriverPrimKey) ON DELETE CASCADE,
foreign key(ThirdPos) references Drivers(DriverPrimKey) ON DELETE CASCADE,
foreign key(FourthPos) references Drivers(DriverPrimKey) ON DELETE CASCADE,
foreign key(FifthPos) references Drivers(DriverPrimKey) ON DELETE CASCADE,
foreign key(SixthPos) references Drivers(DriverPrimKey) ON DELETE CASCADE,
foreign key(SeventhPos) references Drivers(DriverPrimKey) ON DELETE CASCADE,
foreign key(EightPos) references Drivers(DriverPrimKey) ON DELETE CASCADE,
foreign key(NinethPos) references Drivers(DriverPrimKey) ON DELETE CASCADE,
foreign key(TenthPos) references Drivers(DriverPrimKey) ON DELETE CASCADE,
foreign key(EleventhPos) references Drivers(DriverPrimKey) ON DELETE CASCADE,
foreign key(TwelftPos) references Drivers(DriverPrimKey) ON DELETE CASCADE,
foreign key(ThirteenthPos) references Drivers(DriverPrimKey) ON DELETE CASCADE,
foreign key(FourteenthPos) references Drivers(DriverPrimKey) ON DELETE CASCADE,
foreign key(FifteenthPos) references Drivers(DriverPrimKey) ON DELETE CASCADE,
foreign key(SixteenthPos) references Drivers(DriverPrimKey) ON DELETE CASCADE,
foreign key(SeventeenthPos) references Drivers(DriverPrimKey) ON DELETE CASCADE,
foreign key(EighteenthPos) references Drivers(DriverPrimKey) ON DELETE CASCADE,
foreign key(NineteenthPos) references Drivers(DriverPrimKey) ON DELETE CASCADE,
foreign key(TwentiethPos) references Drivers(DriverPrimKey) ON DELETE CASCADE,
foreign key(TwentiethFirstPos) references Drivers(DriverPrimKey) ON DELETE CASCADE,
foreign key(TwentiethSecondPos) references Drivers(DriverPrimKey) ON DELETE CASCADE
);

select * from circuits;
select * from Drivers;
select * from DriverRaceWeekend;
select * from RaceCalender;
select * from Predictions;
select * from PredictionsLeague;

SELECT user,host,password FROM mysql.user;

select GrandPrix, RaceDate, AbbreviatedName, StartPos, DriverPrimKey, RaceCalenderPrimKey from F1Prediction.RaceCalender inner join DriverRaceWeekend on DriverRaceWeekend.RaceCalenderID = RaceCalenderPrimKey inner join Drivers on Drivers.DriverPrimKey = DriverRaceWeekend.DriversID where RaceCalenderPrimKey = 106 order by DriverRaceWeekend.CarNumber;

select CircuitName, Firstyear, Lastyear, Region, Country, GrandsPrixHeld, Length, Turns, LapRecord, LapRecordHolder, Description, ImageLink from F1Prediction.circuits;

update circuits set ImageLink = 'Valencia.svg' where CircuitPrimKey = 4;

select * from DriverRaceWeekend LIMIT 0, 3000;

select * from RaceCalender;

select * from Predictions;

select * from PredictionsLeague;

select * from Drivers;

update PredictionsLeague set LeaguePoints = 20 where PredictionsLeaguePrimKey = 7;

Select  AbbreviatedName from F1Prediction.DriverRaceWeekend inner join RaceCalender on RaceCalenderID = RaceCalender.RaceCalenderPrimKey inner join circuits on CircuitID = circuits.CircuitPrimKey inner join Drivers on DriversID = Drivers.DriverPrimKey where RaceCalender.Season = 2012 AND circuits.CircuitName = 'Hungaroring';

select GrandPrix, RaceDate, AbbreviatedName, StartPos, DriverPrimKey, RaceCalenderPrimKey, FirstPos,SecondPos,ThirdPos,FourthPos,FifthPos,SixthPos,SeventhPos,EightPos,NinethPos,TenthPos,EleventhPos,TwelftPos,ThirteenthPos,FourteenthPos,FifteenthPos,SixteenthPos,SeventeenthPos,EighteenthPos,NineteenthPos,TwentiethPos,TwentiethFirstPos,TwentiethSecondPos from F1Prediction.RaceCalender inner join DriverRaceWeekend on DriverRaceWeekend.RaceCalenderID = RaceCalenderPrimKey inner join Drivers on Drivers.DriverPrimKey = DriverRaceWeekend.DriversID inner join Predictions on RaceCalenderPrimKey = Predictions.RaceCalenderID inner join PredictionsLeague on Predictions.PredictionLeagueID = PredictionsLeague.PredictionsLeaguePrimKey where RaceCalenderPrimKey = 106 AND UserEamil = 'eoinfarrell23@gmail.com' order by DriverRaceWeekend.CarNumber