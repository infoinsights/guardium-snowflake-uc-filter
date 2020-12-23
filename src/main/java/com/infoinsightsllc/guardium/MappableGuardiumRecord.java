package com.infoinsightsllc.guardium;

import com.ibm.guardium.universalconnector.common.structures.Accessor;
import com.ibm.guardium.universalconnector.common.structures.Construct;
import com.ibm.guardium.universalconnector.common.structures.Data;
import com.ibm.guardium.universalconnector.common.structures.ExceptionRecord;
import com.ibm.guardium.universalconnector.common.structures.Record;
import com.ibm.guardium.universalconnector.common.structures.SessionLocator;
import com.ibm.guardium.universalconnector.common.structures.Time;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class MappableGuardiumRecord {
    public static final String UNKNOWN_STRING = "";
    private static final DateTimeFormatterBuilder dateTimeFormatterBuilder = new DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS[[XXX][X]]"));
    private static final DateTimeFormatter DATE_TIME_FORMATTER = dateTimeFormatterBuilder.toFormatter();
    private SessionLocator mapSessionLocator;
    private Record guardRecord;
    private Accessor accessor;
    private Data data;
    private Construct construct;
    private ExceptionRecord exceptionRecord;
    private static final int MAX_PORT = 65535;

    public MappableGuardiumRecord() {
        super();

        guardRecord = new Record();

        guardRecord.setSessionId(UNKNOWN_STRING);
        guardRecord.setDbName(UNKNOWN_STRING);
        mapSessionLocator = new SessionLocator();
        guardRecord.setSessionLocator(mapSessionLocator);

        mapSessionLocator.setIpv6(false);

        mapSessionLocator.setClientIp("0.0.0.0");
        mapSessionLocator.setClientPort(SessionLocator.PORT_DEFAULT);
        mapSessionLocator.setClientIpv6(Parser.UNKOWN_STRING);

        mapSessionLocator.setServerIpv6(Parser.UNKOWN_STRING);
        mapSessionLocator.setServerPort(SessionLocator.PORT_DEFAULT);
        mapSessionLocator.setServerIp("0.0.0.0");

        accessor = new Accessor();
        accessor.setDbProtocol(Parser.DATA_PROTOCOL_STRING);
        accessor.setServerType(Parser.SERVER_TYPE_STRING);
        accessor.setServerHostName(Parser.UNKOWN_STRING);
        accessor.setSourceProgram(Parser.UNKOWN_STRING);
        accessor.setLanguage(Accessor.LANGUAGE_FREE_TEXT_STRING);
        accessor.setDataType(Accessor.DATA_TYPE_GUARDIUM_SHOULD_PARSE_SQL);

        accessor.setClient_mac(Parser.UNKOWN_STRING);
        accessor.setClientHostName(Parser.UNKOWN_STRING);
        accessor.setClientOs(Parser.UNKOWN_STRING);
        accessor.setCommProtocol(Parser.UNKOWN_STRING);
        accessor.setDbProtocolVersion(Parser.UNKOWN_STRING);
        accessor.setOsUser(Parser.UNKOWN_STRING);
        accessor.setServerDescription(Parser.UNKOWN_STRING);
        accessor.setServerOs(Parser.UNKOWN_STRING);
        accessor.setServiceName(Parser.UNKOWN_STRING);

        data = new Data();
        data.setOriginalSqlCommand(Parser.UNKOWN_STRING);
        guardRecord.setData(data);

        construct = new Construct();
        construct.setFullSql(Parser.UNKOWN_STRING);
        construct.setRedactedSensitiveDataSql(Parser.UNKOWN_STRING);
        data.setConstruct(construct);

        exceptionRecord = new ExceptionRecord();
        guardRecord.setException(exceptionRecord);

        guardRecord.setAccessor(accessor);
    }

    public void setMappedField(String field, Object value) throws IOException {

        switch(field) 
        { 
            case "session_id": 
                guardRecord.setSessionId(value.toString());
                
                String sessionID = value.toString();
                guardRecord.setSessionId(sessionID);

                SessionLocator sessionLocator = new SessionLocator();
                sessionLocator.setClientIpv6(Parser.UNKOWN_STRING);
                sessionLocator.setServerIpv6(Parser.UNKOWN_STRING);
                sessionLocator.setClientIp("0.0.0.0");
                sessionLocator.setServerIp("0.0.0.0");

                long numID = Long.parseLong(sessionID);
                sessionLocator.setClientPort(Math.toIntExact(numID % MAX_PORT));
                sessionLocator.setServerPort(Math.toIntExact((numID / MAX_PORT)  % MAX_PORT));

                guardRecord.setSessionLocator(sessionLocator);

                break; 
            case "warehouse_name": 
                accessor.setServiceName(value.toString());
                break;
            case "role_name":
                guardRecord.setAppUserName(value.toString());
                accessor.setOsUser(value.toString());
                break;
            case "start_time":
                guardRecord.setTime(getTime(value));
                break;
            case "host":
                accessor.setClientHostName(value.toString());
                break;
            case "database_name":
                guardRecord.setDbName(value.toString());
                break;
            case "user_name":
                accessor.setDbUser(value.toString());
                break;
            case "release_version": 
                accessor.setDbProtocolVersion(value.toString());
                break;
            case "server_host_name":
                accessor.setServerHostName(value.toString());
                break;
            case "query_text":
                data.setOriginalSqlCommand(value.toString());
                construct.setFullSql(value.toString());
                construct.setRedactedSensitiveDataSql(value.toString());
                exceptionRecord.setSqlString(value.toString());
                break;
            case "execution_status":
                if(value.toString().equals("SUCCESS")){
                    guardRecord.setException(null);
                }
                else{
                    exceptionRecord.setExceptionTypeId(Parser.EXCEPTION_TYPE_AUTHORIZATION_STRING);
                }
                break;
            case "error_message":
                exceptionRecord.setDescription(value.toString());
                break;
            default:
                break;
        }
    }

    public Time getTime(Object timefromevent){
        ZonedDateTime date = ZonedDateTime.parse(timefromevent.toString(), DATE_TIME_FORMATTER);
        long millis = date.toInstant().toEpochMilli();
        int  minOffset = date.getOffset().getTotalSeconds()/60;
        return new Time(millis, minOffset, 0);
    }

    public Record getGuardRecord(){
        return guardRecord;
    }

}
