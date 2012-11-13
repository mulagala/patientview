/**
      Patch 2: Combine unit and tbl_centre tables
 */

SET FOREIGN_KEY_CHECKS = 0;

ALTER TABLE unit MODIFY unitcode varchar(100) NOT NULL;
ALTER TABLE unit MODIFY tenancy_id bigint(20) NULL;

alter table unit add column country varchar(100);
alter table tbl_centres add column unitcode varchar(100);

update tbl_centres set unitcode = "11023" where cid = 1;
update tbl_centres set unitcode = "RQ3" where cid = 2;
update tbl_centres set unitcode = "RA723" where cid = 3;
update tbl_centres set unitcode = "RWM51" where cid = 4;
update tbl_centres set unitcode = "SGC02" where cid = 5;
update tbl_centres set unitcode = "99RQR13" where cid = 6;
update tbl_centres set unitcode = "RBS25" where cid = 7;
update tbl_centres set unitcode = "RJ122" where cid = 8;
update tbl_centres set unitcode = "RP4" where cid = 9;
update tbl_centres set unitcode = "RW3RM" where cid = 10;
update tbl_centres set unitcode = "99RHM01" where cid = 13;
update tbl_centres set unitcode = "99RTD01" where cid = 11;
update tbl_centres set unitcode = "99RCSLB" where cid = 12;
update tbl_centres set unitcode = "RENALREG" where cid = 14;
update tbl_centres set unitcode = "DEMO" where cid = 15;
update tbl_centres set unitcode = "UNKNOWN" where cid = 16;
update tbl_centres set unitcode = "VELLORE" where cid = 55;
update tbl_centres set unitcode = "BANGALORE" where cid = 56;
update tbl_centres set unitcode = "NEWDEHLI" where cid = 57;
update tbl_centres set unitcode = "TERAN" where cid = 58;
update tbl_centres set unitcode = "DEMOENTRY" where cid = 99;

update tbl_centres set CCOUNTRY = 1 where cid = 16;
update tbl_centres set CABBREV = "UNKNOWN" where cid = 16;
update tbl_centres set CCOUNTRY = 2 where cid = 99;

update unit set id = 5, country =1 where unitcode = "SGC02";
update unit set id = 17, country =1 where unitcode = "2020";
update unit set id = 18, country =1 where unitcode = "7021";
update unit set id = 19, country =1 where unitcode = "45020";
update unit set id = 20, country =1 where unitcode = "48021";
update unit set id = 21, country =1 where unitcode = "ALPORT";
update unit set id = 22, country =1 where unitcode = "CCL";
update unit set id = 23, country =1 where unitcode = "CHI";
update unit set id = 24, country =1 where unitcode = "DUMMY";
update unit set id = 25, country =1 where unitcode = "MPGN";
update unit set id = 26, country =1 where unitcode = "PATIENT";
update unit set id = 27, country =1 where unitcode = "RAE05";
update unit set id = 28, country =1 where unitcode = "RAJ01";
update unit set id = 29, country =1 where unitcode = "RAL01";
update unit set id = 30, country =1 where unitcode = "RAQ01";
update unit set id = 31, country =1 where unitcode = "RAZ";
update unit set id = 32, country =1 where unitcode = "RBL14";
update unit set id = 33, country =1 where unitcode = "RBN01";
update unit set id = 34, country =1 where unitcode = "RCB55";
update unit set id = 35, country =1 where unitcode = "RCJAT";
update unit set id = 36, country =1 where unitcode = "RDDH0";
update unit set id = 37, country =1 where unitcode = "REE01";
update unit set id = 38, country =1 where unitcode = "REF12";
update unit set id = 39, country =1 where unitcode = "RF201";
update unit set id = 40, country =1 where unitcode = "RFBAK";
update unit set id = 41, country =1 where unitcode = "RFPFG";
update unit set id = 42, country =1 where unitcode = "RGT01";
update unit set id = 43, country =1 where unitcode = "RGU01";
update unit set id = 44, country =1 where unitcode = "RH641";
update unit set id = 45, country =1 where unitcode = "RH8";
update unit set id = 46, country =1 where unitcode = "RHU02";
update unit set id = 47, country =1 where unitcode = "RHW01";
update unit set id = 48, country =1 where unitcode = "RJ100";
update unit set id = 49, country =1 where unitcode = "RJ701";
update unit set id = 50, country =1 where unitcode = "RJE01";
update unit set id = 51, country =1 where unitcode = "RJR05";
update unit set id = 52, country =1 where unitcode = "RJZ";
update unit set id = 53, country =1 where unitcode = "RK7CC";
update unit set id = 54, country =1 where unitcode = "RK950";
update unit set id = 88, country =1 where unitcode = "RKB01";
update unit set id = 89, country =1 where unitcode = "RL7";
update unit set id = 90, country =1 where unitcode = "RLGAY";
update unit set id = 91, country =1 where unitcode = "RLNGH";
update unit set id = 59, country =1 where unitcode = "RM102";
update unit set id = 60, country =1 where unitcode = "RM301";
update unit set id = 61, country =1 where unitcode = "RM574";
update unit set id = 62, country =1 where unitcode = "RMF01";
update unit set id = 63, country =1 where unitcode = "RNA03";
update unit set id = 64, country =1 where unitcode = "RNX02";
update unit set id = 65, country =1 where unitcode = "RP5";
update unit set id = 66, country =1 where unitcode = "RQ601";
update unit set id = 67, country =1 where unitcode = "RQ617";
update unit set id = 68, country =1 where unitcode = "RQ8L0";
update unit set id = 69, country =1 where unitcode = "RQHC7";
update unit set id = 70, country =1 where unitcode = "RQR00";
update unit set id = 71, country =1 where unitcode = "RRBBV";
update unit set id = 72, country =1 where unitcode = "RRK02";
update unit set id = 73, country =1 where unitcode = "RSC02";
update unit set id = 74, country =1 where unitcode = "RTD01";
update unit set id = 75, country =1 where unitcode = "RVVKC";
update unit set id = 76, country =1 where unitcode = "RW402";
update unit set id = 77, country =1 where unitcode = "RX1CC";
update unit set id = 78, country =1 where unitcode = "SAC02";
update unit set id = 79, country =1 where unitcode = "SFC01";
update unit set id = 80, country =1 where unitcode = "SGC04";
update unit set id = 81, country =1 where unitcode = "SGC05";
update unit set id = 82, country =1 where unitcode = "SHC01";
update unit set id = 83, country =1 where unitcode = "SLC01";
update unit set id = 84, country =1 where unitcode = "SNC01";
update unit set id = 85, country =1 where unitcode = "SRNS";
update unit set id = 86, country =1 where unitcode = "STC01";
update unit set id = 87, country =1 where unitcode = "SYC01";

delete from tbl_centres where unitcode = '11023';
delete from tbl_centres where unitcode = 'RQ3';
delete from tbl_centres where unitcode = 'RA723';
delete from tbl_centres where unitcode = 'RWM51';
delete from tbl_centres where unitcode = 'SGC02';
delete from tbl_centres where unitcode = '99RQR13';
delete from tbl_centres where unitcode = 'RBS25';
delete from tbl_centres where unitcode = 'RJ122';
delete from tbl_centres where unitcode = 'RP4';
delete from tbl_centres where unitcode = 'RW3RM';
delete from tbl_centres where unitcode = '99RHM01';

insert into unit (unitcode, id, name, shortname, country, sourcetype)
select unitcode, cID, cName, cAbbrev, cCountry, "radargroup"
from tbl_centres;

ALTER TABLE unit AUTO_INCREMENT = 100;

ALTER TABLE UNIT ADD UNIQUE (UNITCODE);
CREATE INDEX UnitIDIndex ON UNIT (ID);
CREATE INDEX UnitCodeIndex ON UNIT (UNITCODE);

SET FOREIGN_KEY_CHECKS = 1;