/*
 *******************************************************************************
 *
 * Copyright (C) 2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.apis;

public class Constants {
    /**
     * InternalAPI
     */
    public static class InternalAPI {

        public static final class ConfigurationMsg {
            public static final String NAME = ConfigurationMsg.class.getSimpleName();
            public static final Class CLASS = com.dialog.wearables.apis.internal.ConfigurationMsg.class;
        }

        public static final class DataMsg {
            public static final String NAME = DataMsg.class.getSimpleName();
            public static final Class CLASS = com.dialog.wearables.apis.internal.DataMsg.class;
        }
    }

    /**
     * CloudAPI
     */
    public static class CloudAPI {

        public static class Rest {

            public static class Routes {

                public static class DIALOG {
                    public static final String NAME = DIALOG.class.getSimpleName().toLowerCase();
                }

                public static class EDGE {
                    public static final String NAME = EDGE.class.getSimpleName().toLowerCase();
                }

                public static class IOTAPPS {
                    public static final String NAME = IOTAPPS.class.getSimpleName().toLowerCase();
                }

                public static class MGMT {
                    public static final String NAME = MGMT.class.getSimpleName().toLowerCase();
                }
            }

            public static class Subroutes {

                public static class Management {
                    public static class G_EKID {
                        public static final String NAME = G_EKID.class.getSimpleName().toLowerCase();
                    }

                    public static class P_WEBAPPLINK {
                        public static final String NAME = P_WEBAPPLINK.class.getSimpleName().toLowerCase();
                    }

                    public static class G_USERID {
                        public static final String NAME = G_USERID.class.getSimpleName().toLowerCase();
                    }

                    public static class G_USERIDBYTOKEN {
                        public static final String NAME = G_USERIDBYTOKEN.class.getSimpleName().toLowerCase();
                    }

                    public static class P_DEVAPPLINK {
                        public static final String NAME = P_DEVAPPLINK.class.getSimpleName().toLowerCase();
                    }

                    public static class P_SETDEVICE {
                        public static final String NAME = P_SETDEVICE.class.getSimpleName().toLowerCase();
                    }

                    public static class P_SETIOTAPPINFO {
                        public static final String NAME = P_SETIOTAPPINFO.class.getSimpleName().toLowerCase();
                    }
                }

                public static class IotApps {

                    public static class HISTORICAL {
                        public static final String NAME = HISTORICAL.class.getSimpleName().toLowerCase();
                    }

                    public static class G_TEMPERATURE {
                        public static final String NAME = G_TEMPERATURE.class.getSimpleName().toLowerCase();
                    }

                    public static class G_PRESSURE {
                        public static final String NAME = G_PRESSURE.class.getSimpleName().toLowerCase();
                    }

                    public static class G_HUMIDITY {
                        public static final String NAME = G_HUMIDITY.class.getSimpleName().toLowerCase();
                    }

                    public static class G_AIRQUALITY {
                        public static final String NAME = G_AIRQUALITY.class.getSimpleName().toLowerCase();
                    }

                    public static class G_BRIGHTNESS {
                        public static final String NAME = G_BRIGHTNESS.class.getSimpleName().toLowerCase();
                    }

                    public static class G_PROXIMITY {
                        public static final String NAME = G_PROXIMITY.class.getSimpleName().toLowerCase();
                    }

                    public static class ALERTING {
                        public static final String NAME = ALERTING.class.getSimpleName().toLowerCase();
                    }

                    public static class P_SETRULE {
                        public static final String NAME = P_SETRULE.class.getSimpleName().toLowerCase();
                    }

                    public static class G_GETRULES {
                        public static final String NAME = G_GETRULES.class.getSimpleName().toLowerCase();
                    }

                    public static class CONTROL {
                        public static final String NAME = CONTROL.class.getSimpleName().toLowerCase();
                    }

                    public static class TRACKING {
                        public static final String NAME = TRACKING.class.getSimpleName().toLowerCase();
                    }

                    public static class P_SETASSETTAG {
                        public static final String NAME = P_SETASSETTAG.class.getSimpleName().toLowerCase();
                    }

                    public static class ALEXA {
                        public static final String NAME = ALEXA.class.getSimpleName().toLowerCase();
                    }

                    public static class P_AMAZONINFO {
                        public static final String NAME = P_AMAZONINFO.class.getSimpleName().toLowerCase();
                    }

                    public static class GAME3D {
                        public static final String NAME = GAME3D.class.getSimpleName().toLowerCase();
                    }

                    public static class G_IFTTTAPIKEY {
                        public static final String NAME = G_IFTTTAPIKEY.class.getSimpleName().toLowerCase();
                    }

                    public static class P_IFTTTAPIKEY {
                        public static final String NAME = P_IFTTTAPIKEY.class.getSimpleName().toLowerCase();
                    }
                }
            }

            public static class Parameters {
                public static class TOKEN {
                    public static final String NAME = "Token";
                }

                public static class APPID {
                    public static final String NAME = APPID.class.getSimpleName();
                }

                public static class UserId {
                    public static final String NAME = "UserId";
                }

                public static class EKID {
                    public static final String NAME = EKID.class.getSimpleName();
                }

                public static class StartDate {
                    public static final String NAME = "StartDate";
                }

                public static class EndDate {
                    public static final String NAME = "EndDate";
                }
            }

            public static class Server {
                public static final String SCHEME = Constants.Endpoints.SSL ? "https" : "http";
                public static final String URL = Constants.Endpoints.SSL ?
                        Endpoints.YODIWO_SERVICE :
                        Endpoints.YODIWO_SERVICE + ":3320" ;
            }
        }

        public static class Ifttt {

            public static class Routes {
                public static final String TRIGGER_BUTTON_WITH_KEY = "trigger/button/with/key";
                public static final String TRIGGER_TEMPERATURE_WITH_KEY = "trigger/temperature/with/key";
                public static final String TRIGGER_HUMIDITY_WITH_KEY = "trigger/humidity/with/key";
                public static final String TRIGGER_PRESSURE_WITH_KEY = "trigger/pressure/with/key";
            }

            public static class Server {
                public static final String URI = Endpoints.IFTTT;
            }
        }

        public static class Mqtt {

            private static class SubTopic {
                private static class DIALOGEK {
                    public static final String NAME = DIALOGEK.class.getSimpleName().toLowerCase();
                }

                private static class GW {
                    public static final String NAME = GW.class.getSimpleName().toLowerCase();
                }

                private static class TOSERVICE {
                    public static final String NAME = TOSERVICE.class.getSimpleName().toLowerCase();
                }

                private static class FROMSERVICE {
                    public static final String NAME = FROMSERVICE.class.getSimpleName().toLowerCase();
                }
            }

            public static class Topic {
                public static final String PUBLISH = SubTopic.DIALOGEK.NAME + "/" + SubTopic.GW.NAME + "/" + SubTopic.TOSERVICE.NAME;
                public static final String SUBSCRIBE = SubTopic.DIALOGEK.NAME + "/" + SubTopic.FROMSERVICE.NAME;
            }

            public static class Credentials {
                public static final String USERNAME = "DialogEdgeDevices";
                public static final String PASSWORD = "Tamed.Piranha!";
            }

            public static class Server {
                public static final String URL = Endpoints.SSL ?
                        "ssl://" + Endpoints.YODIWO_MQTT + ":8883" :
                        "tcp://" + Endpoints.YODIWO_MQTT + ":1883";
            }
        }
    }

    /**
     * Endpoints
     */
    public static class Endpoints {
        private static final boolean SSL = true;
        private static final String YODIWO_SERVICE = SSL ? "service.dialog-cloud.com" : "10.30.254.100";
        private static final String YODIWO_MQTT = "mqtt.dialog-cloud.com";
        private static final String IFTTT = "maker.ifttt.com";
        public static final String YODIBOARDS = "app.dialog-cloud.com";
    }
}
