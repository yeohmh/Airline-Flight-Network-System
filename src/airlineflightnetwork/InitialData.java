package airlineflightnetwork;

public final class InitialData {
    private InitialData() { }

    public static AirlineNetwork createNetwork() {
        AirlineNetwork n = new AirlineNetwork();
        n.setOperationMessagesEnabled(false);
        Object[][] airports = {
            {"KUL","Kuala Lumpur International Airport","Kuala Lumpur","Malaysia"},
            {"PEN","Penang International Airport","Penang","Malaysia"},
            {"LGK","Langkawi International Airport","Langkawi","Malaysia"},
            {"JHB","Senai International Airport","Johor Bahru","Malaysia"},
            {"KBR","Sultan Ismail Petra Airport","Kota Bharu","Malaysia"},
            {"KCH","Kuching International Airport","Kuching","Malaysia"},
            {"BKI","Kota Kinabalu International Airport","Kota Kinabalu","Malaysia"},
            {"SIN","Singapore Changi Airport","Singapore","Singapore"},
            {"BKK","Suvarnabhumi Airport","Bangkok","Thailand"},
            {"CGK","Soekarno-Hatta International Airport","Jakarta","Indonesia"},
            {"MNL","Ninoy Aquino International Airport","Manila","Philippines"},
            {"HKG","Hong Kong International Airport","Hong Kong","China"},
            {"TPE","Taiwan Taoyuan International Airport","Taipei","Taiwan"},
            {"NRT","Narita International Airport","Tokyo","Japan"},
            {"ICN","Incheon International Airport","Seoul","South Korea"}
        };
        for (Object[] a : airports) n.addAirport(new Airport((String)a[0],(String)a[1],(String)a[2],(String)a[3]));

        Object[][] flights = {
            {"KUL","PEN",325,120.0,60},{"KUL","JHB",290,95.0,55},{"KUL","KBR",440,130.0,65},
            {"KUL","KCH",975,220.0,110},{"KUL","BKI",1630,310.0,165},{"KUL","SIN",300,140.0,65},
            {"KUL","BKK",1215,320.0,130},{"KUL","CGK",1180,280.0,125},{"KUL","HKG",2540,650.0,240},
            {"KUL","TPE",3250,760.0,285},{"KUL","NRT",5400,1800.0,420},
            {"PEN","KUL",325,135.0,65},{"PEN","LGK",130,65.0,35},{"PEN","SIN",600,180.0,90},{"PEN","BKK",945,210.0,105},
            {"LGK","KUL",455,105.0,65},{"LGK","PEN",130,70.0,35},
            {"JHB","KUL",290,100.0,55},{"JHB","KCH",735,190.0,90},{"JHB","SIN",50,55.0,35},
            {"KBR","KUL",440,125.0,65},{"KBR","PEN",225,85.0,50},
            {"KCH","KUL",975,230.0,110},{"KCH","JHB",735,200.0,90},{"KCH","BKI",805,185.0,95},{"KCH","SIN",710,210.0,95},
            {"BKI","KUL",1630,330.0,165},{"BKI","KCH",805,195.0,95},{"BKI","MNL",1090,260.0,120},{"BKI","HKG",1810,420.0,185},
            {"SIN","KUL",300,150.0,65},{"SIN","JHB",50,60.0,35},{"SIN","CGK",885,190.0,105},{"SIN","BKK",1430,300.0,145},
            {"SIN","HKG",2570,570.0,235},{"SIN","TPE",3220,690.0,275},{"SIN","NRT",5320,1250.0,410},
            {"BKK","KUL",1215,300.0,130},{"BKK","PEN",945,230.0,105},{"BKK","SIN",1430,310.0,145},
            {"BKK","HKG",1680,360.0,165},{"BKK","TPE",2490,450.0,220},{"BKK","NRT",4650,780.0,365},
            {"CGK","KUL",1180,290.0,125},{"CGK","SIN",885,200.0,105},
            {"MNL","BKI",1090,270.0,120},{"MNL","HKG",1145,260.0,135},{"MNL","TPE",1170,250.0,130},{"MNL","NRT",3050,620.0,260},
            {"HKG","KUL",2540,620.0,240},{"HKG","BKI",1810,400.0,185},{"HKG","SIN",2570,590.0,235},
            {"HKG","BKK",1680,370.0,165},{"HKG","TPE",810,190.0,105},{"HKG","NRT",2900,480.0,245},
            {"TPE","KUL",3250,740.0,285},{"TPE","SIN",3220,710.0,275},{"TPE","BKK",2490,470.0,220},
            {"TPE","HKG",810,200.0,105},{"TPE","NRT",2180,420.0,190},{"TPE","ICN",1460,330.0,150},
            {"NRT","KUL",5400,1650.0,430},{"NRT","SIN",5320,1180.0,415},{"NRT","BKK",4650,760.0,365},
            {"NRT","HKG",2900,500.0,250},{"NRT","TPE",2180,440.0,195},{"NRT","ICN",1260,310.0,145},
            {"ICN","TPE",1460,340.0,150},{"ICN","NRT",1260,320.0,145},{"ICN","HKG",2070,430.0,210}
        };
        for (Object[] f : flights) n.addFlight((String)f[0],(String)f[1],(Integer)f[2],(Double)f[3],(Integer)f[4]);
        n.setOperationMessagesEnabled(true);
        return n;
    }
}
