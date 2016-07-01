/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.Choreographer;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import armyc2.c2sd.renderer.utilities.MilStdAttributes;
import armyc2.c2sd.renderer.utilities.ModifiersUnits;
import gov.nasa.worldwind.Navigator;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.layer.ShowTessellationLayer;
import gov.nasa.worldwindx.milstd2525.MilStd2525;
import gov.nasa.worldwindx.milstd2525.MilStd2525LevelOfDetailSelector;
import gov.nasa.worldwindx.milstd2525.MilStd2525Placemark;

public class PlacemarksMilStd2525StressActivity extends GeneralGlobeActivity implements Choreographer.FrameCallback {

    protected static final int NUM_PLACEMARKS = 10000;

    private final static String[] StandardIdentities = {
        "P",    // Pending
        "U",    // Unknown
        "F",    // Friend
        "N",    // Neutral
        "H",    // Hostile
        "A",    // Assumed Friend
        "S"};   // Suspect

    private final static String[] BattleDimensions = {
        "Z",    // Unknown
        "P",    // Space
        "A",    // Air
        "G",    // Ground
        "S",    // Sea surface
        "U",    // Subsurface
        "F"};   // SOF

    private final static String[] StatusCodes = {
        "A",    // Anticipated
        "P",    // Present
        "C",    // Present/Fully Capable
        "D",    // Present/Damaged
        "X",    // Present/Destroyed
        "F"};   // Present/Full to Capacity

    private final static String[] WarfightingUnknownFunctionIDs = {
        "------"};

    private final static String[] WarfightingSpaceFunctionIDs = {
        "------",
        "S-----",
        "V-----",
        "T-----",
        "L-----"};

    private final static String[] WarfightingAirFunctionIDs = {
        "------",
        "C-----",
        "M-----",
        "MF----",
        "MFB---",
        "MFF---", "MFFI--",
        "MFT---",
        "MFA---",
        "MFL---",
        "MFK---", "MFKB--", "MFKD--",
        "MFC---", "MFCL--", "MFCM--", "MFCH--",
        "MFJ---",
        "MFO---",
        "MFR---", "MFRW--", "MFRZ--", "MFRX--",
        "MFP---", "MFPN--", "MFPM--",
        "MFU---", "MFUL--", "MFUM--", "MFUH--",
        "MFY---",
        "MFH---",
        "MFD---",
        "MFQ---",
        "MFQA--",
        "MFQB--",
        "MFQC--",
        "MFQD--",
        "MFQF--",
        "MFQH--",
        "MFQJ--",
        "MFQK--",
        "MFQL--",
        "MFQM--",
        "MFQI--",
        "MFQN--",
        "MFQP--",
        "MFQR--",
        "MFQRW-", "MFQRZ-", "MFQRX-",
        "MFQS--",
        "MFQT--",
        "MFQU--",
        "MFQY--",
        "MFQO--",
        "MFS---",
        "MFM---",
        "MH----",
        "MHA---",
        "MHS---",
        "MHU---", "MHUL--", "MHUM--", "MHUH--",
        "MHI---",
        "MHH---",
        "MHR---",
        "MHQ---",
        "MHC---",
        "MHCL--",
        "MHCM--",
        "MHCH--",
        "MHT---",
        "MHO---",
        "MHM---",
        "MHD---",
        "MHK---",
        "MHJ---",
        "ML----",
        "MV----",
        "ME----",
        "W-----",
        "WM----",
        "WMS---",
        "WMSS--",
        "WMSA--",
        "WMSU--",
        "WMSB--",
        "WMA---",
        "WMAS--",
        "WMAA--",
        "WMAP--",
        "WMU---",
        "WMCM--",
        "WMB---",
        "WB----",
        "WD----",
        "C-----",
        "CF----",
        "CH----",
        "CL----"};

    private final static String[] WarfightingGroundFunctionIDs = {
        "------",
        "U-----",
        "UC----",
        "UCD---",
        "UCDS--",
        "UCDSC-",
        "UCDSS-",
        "UCDSV-",
        "UCDM--",
        "UCDML-",
        "UCDMLA",
        "UCDMM-",
        "UCDMH-",
        "UCDH--",

        "UCDHH-",
        "UCDHP-",
        "UCDG--",
        "UCDC--",
        "UCDT--",
        "UCDO--",
        "UCA---",
        "UCAT--",
        "UCATA-",
        "UCATW-",
        "UCATWR",
        "UCATL-",
        "UCATM-",
        "UCATH-",
        "UCATR-",
        "UCAW--",
        "UCAWS-",
        "UCAWA-",
        "UCAWW-",
        "UCAWWR",
        "UCAWL-",
        "UCAWM-",
        "UCAWH-",
        "UCAWR-",
        "UCAA--",

        "UCAAD-",
        "UCAAL-",
        "UCAAM-",
        "UCAAS-",
        "UCAAU-",
        "UCAAC-",
        "UCAAA-",
        "UCAAAT",
        "UCAAAW",
        "UCAAAS",
        "UCAAO-",
        "UCAAOS",
        "UCV---",
        "UCVF--",
        "UCVFU-",
        "UCVFA-",
        "UCVFR-",
        "UCVR--",
        "UCVRA-",
        "UCVRS-",
        "UCVRW-",
        "UCVRU-",
        "UCVRUL",
        "UCVRUM",
        "UCVRUH",

        "UCVRUC",
        "UCVRUE",
        "UCVRM-",
        "UCVS--",
        "UCVC--",
        "UCVV--",
        "UCVU--",
        "UCVUF-",
        "UCVUR-",
        "UCI---",
        "UCIL--",
        "UCIM--",
        "UCIO--",
        "UCIA--",
        "UCIS--",
        "UCIZ--",
        "UCIN--",
        "UCII--",
        "UCIC--",
        "UCE---",
        "UCEC--",
        "UCECS-",
        "UCECA-",
        "UCECC-",

        "UCECL-",
        "UCECM-",
        "UCECH-",
        "UCECT-",
        "UCECW-",
        "UCECO-",
        "UCECR-",
        "UCEN--",
        "UCENN-",
        "UCF---",
        "UCFH--",
        "UCFHE-",
        "UCFHS-",
        "UCFHA-",
        "UCFHC-",
        "UCFHO-",
        "UCFHL-",
        "UCFHM-",
        "UCFHH-",
        "UCFHX-",
        "UCFR--",
        "UCFRS-",
        "UCFRSS",
        "UCFRSR",
        "UCFRST",

        "UCFRM-",
        "UCFRMS",
        "UCFRMR",
        "UCFRMT",
        "UCFT--",
        "UCFTR-",
        "UCFTS-",
        "UCFTF-",
        "UCFTC-",
        "UCFTCD",
        "UCFTCM",
        "UCFTA-",
        "UCFM--",
        "UCFMS-",
        "UCFMW-",
        "UCFMT-",
        "UCFMTA",
        "UCFMTS",
        "UCFMTC",
        "UCFMTO",
        "UCFML-",
        "UCFS--",
        "UCFSS-",
        "UCFSA-",
        "UCFSL-",

        "UCFSO-",
        "UCFO--",
        "UCFOS-",
        "UCFOA-",
        "UCFOL-",
        "UCFOO-",
        "UCR---",
        "UCRH--",
        "UCRV--",
        "UCRVA-",
        "UCRVM-",
        "UCRVG-",
        "UCRVO-",
        "UCRC--",
        "UCRS--",
        "UCRA--",
        "UCRO--",
        "UCRL--",
        "UCRR--",
        "UCRRD-",
        "UCRRF-",
        "UCRRL-",
        "UCRX--",
        "UCM---",

        "UCMT--",
        "UCMS--",
        "UCS---",
        "UCSW--",
        "UCSG--",
        "UCSGD-",
        "UCSGM-",
        "UCSGA-",
        "UCSM--",
        "UCSR--",
        "UCSA--",
        "UU----",
        "UUA---",
        "UUAC--",
        "UUACC-",
        "UUACCK",
        "UUACCM",
        "UUACS-",
        "UUACSM",
        "UUACSA",
        "UUACR-",
        "UUACRW",
        "UUACRS",
        "UUAN--",

        "UUAB--",
        "UUABR-",
        "UUAD--",
        "UUM---",
        "UUMA--",
        "UUMS--",
        "UUMSE-",
        "UUMSEA",
        "UUMSED",
        "UUMSEI",
        "UUMSEJ",
        "UUMSET",
        "UUMSEC",
        "UUMC--",
        "UUMR--",
        "UUMRG-",
        "UUMRS-",
        "UUMRSS",
        "UUMRX-",
        "UUMMO-",
        "UUMO--",
        "UUMT--",
        "UUMQ--",
        "UUMJ--",
        "UUL---",

        "UULS--",
        "UULM--",
        "UULC--",
        "UULF--",
        "UULD--",
        "UUS---",
        "UUSA--",
        "UUSC--",
        "UUSCL-",
        "UUSO--",
        "UUSF--",
        "UUSM--",
        "UUSMS-",
        "UUSML-",
        "UUSMN-",
        "UUSR--",
        "UUSRS-",
        "UUSRT-",
        "UUSRW-",
        "UUSS--",
        "UUSW--",
        "UUSX--",
        "UUI---",
        "UUP---",
        "UUE---",

        "US----",
        "USA---",
        "USAT--",
        "USAC--",
        "USAJ--",
        "USAJT-",
        "USAJC-",
        "USAO--",
        "USAOT-",
        "USAOC-",
        "USAF--",
        "USAFT-",
        "USAFC-",
        "USAS--",
        "USAST-",
        "USASC-",
        "USAM--",
        "USAMT-",
        "USAMC-",
        "USAR--",
        "USART-",
        "USARC-",
        "USAP--",
        "USAPT-",
        "USAPC-",

        "USAPB-",
        "USAPBT",
        "USAPBC",
        "USAPM-",
        "USAPMT",
        "USAPMC",
        "USAX--",
        "USAXT-",
        "USAXC-",
        "USAL--",
        "USALT-",
        "USALC-",
        "USAW--",
        "USAWT-",
        "USAWC-",
        "USAQ--",
        "USAQT-",
        "USAQC-",
        "USM---",
        "USMT--",
        "USMC--",
        "USMM--",
        "USMMT-",
        "USMMC-",
        "USMV--",

        "USMVT-",
        "USMVC-",
        "USMD--",
        "USMDT-",
        "USMDC-",
        "USMP--",
        "USMPT-",
        "USMPC-",
        "USS---",
        "USST--",
        "USSC--",
        "USS1--",
        "USS1T-",
        "USS1C-",
        "USS2--",
        "USS2T-",
        "USS2C-",
        "USS3--",
        "USS3T-",
        "USS3C-",
        "USS3A-",
        "USS3AT",
        "USS3AC",
        "USS4--",
        "USS4T-",

        "USS4C-",
        "USS5--",
        "USS5T-",
        "USS5C-",
        "USS6--",
        "USS6T-",
        "USS6C-",
        "USS7--",
        "USS7T-",
        "USS7C-",
        "USS8--",
        "USS8T-",
        "USS8C-",
        "USS9--",
        "USS9T-",
        "USS9C-",
        "USSX--",
        "USSXT-",
        "USSXC-",
        "USSL--",
        "USSLT-",
        "USSLC-",
        "USSW--",
        "USSWT-",
        "USSWC-",

        "USSWP-",
        "USSWPT",
        "USSWPC",
        "UST---",
        "USTT--",
        "USTC--",
        "USTM--",
        "USTMT-",
        "USTMC-",
        "USTR--",
        "USTRT-",
        "USTRC-",
        "USTS--",
        "USTST-",
        "USTSC-",
        "USTA--",
        "USTAT-",
        "USTAC-",
        "USTI--",
        "USTIT-",
        "USTIC-",
        "USX---",
        "USXT--",
        "USXC--",
        "USXH--",

        "USXHT-",
        "USXHC-",
        "USXR--",
        "USXRT-",
        "USXRC-",
        "USXO--",
        "USXOT-",
        "USXOC-",
        "USXOM-",
        "USXOMT",
        "USXOMC",
        "USXE--",
        "USXET-",
        "USXEC-",
        "UH----",
        "E-----",
        //"EW----",         // icon not used
        "EWM---",
        "EWMA--",
        "EWMAS-",
        "EWMASR",
        "EWMAI-",
        "EWMAIR",
        "EWMAIE",

        "EWMAL-",
        "EWMALR",
        "EWMALE",
        "EWMAT-",
        "EWMATR",
        "EWMATE",
        "EWMS--",
        "EWMSS-",
        "EWMSI-",
        "EWMSL-",
        "EWMT--",
        "EWMTL-",
        "EWMTM-",
        "EWMTH-",
        "EWS---",
        "EWSL--",
        "EWSM--",
        "EWSH--",
        "EWX---",
        "EWXL--",
        "EWXM--",
        "EWXH--",
        "EWT---",
        "EWTL--",
        "EWTM--",

        "EWTH--",
        "EWR---",
        "EWRR--",
        "EWRL--",
        "EWRH--",
        "EWZ---",
        "EWZL--",
        "EWZM--",
        "EWZH--",
        "EWO---",
        "EWOL--",
        "EWOM--",
        "EWOH--",
        "EWH---",
        "EWHL--",
        "EWHLS-",
        "EWHM--",
        "EWHMS-",
        "EWHH--",
        "EWHHS-",
        "EWG---",
        "EWGL--",
        "EWGM--",
        "EWGH--",
        "EWGR--",

        "EWD---",
        "EWDL--",
        "EWDLS-",
        "EWDM--",
        "EWDMS-",
        "EWDH--",
        "EWDHS-",
        "EWA---",
        "EWAL--",
        "EWAM--",
        "EWAH--",
        "EV----",
        "EVA---",
        "EVAT--",
        "EVATL-",
        "EVATLR",
        "EVATM-",
        "EVATMR",
        "EVATH-",
        "EVATHR",
        "EVAA--",
        "EVAAR-",
        "EVAI--",
        "EVAC--",
        "EVAS--",

        "EVAL--",
        "EVU---",
        "EVUB--",
        "EVUS--",
        "EVUSL-",
        "EVUSM-",
        "EVUSH-",
        "EVUL--",
        "EVUX--",
        "EVUR--",
        "EVUT--",
        "EVUTL-",
        "EVUTH-",
        "EVUA--",
        "EVUAA-",
        "EVE---",
        "EVEB--",
        "EVEE--",
        "EVEC--",
        "EVEM--",
        "EVEMV-",
        "EVEML-",
        "EVEA--",
        "EVEAA-",
        "EVEAT-",

        "EVED--",
        "EVEDA-",
        "EVES--",
        "EVER--",
        "EVEH--",
        "EVEF--",
        "EVT---",
        "EVC---",
        "EVCA--",
        "EVCAL-",
        "EVCAM-",
        "EVCAH-",
        "EVCO--",
        "EVCOL-",
        "EVCOM-",
        "EVCOH-",
        "EVCM--",
        "EVCML-",
        "EVCMM-",
        "EVCMH-",
        "EVCU--",
        "EVCUL-",
        "EVCUM-",
        "EVCUH-",
        "EVCJ--",

        "EVCJL-",
        "EVCJM-",
        "EVCJH-",
        "EVCT--",
        "EVCTL-",
        "EVCTM-",
        "EVCTH-",
        "EVCF--",
        "EVCFL-",
        "EVCFM-",
        "EVCFH-",
        "EVM---",
        "EVS---",
        "EVST--",
        "EVSR--",
        "EVSC--",
        "EVSP--",
        "EVSW--",
        "ES----",
        "ESR---",
        "ESE---",
        //"EX----",         // icon not used
        "EXI---",
        "EXL---",
        "EXN---",

        "EXF---",
        "EXM---",
        "EXMC--",
        "EXML--",
        "I-----",
        "IR----",
        "IRM---",
        "IRP---",
        "IRN---",
        "IRNB--",
        "IRNC--",
        "IRNN--",
        "IP----",
        "IPD---",
        "IE----",
        "IU----",
        "IUR---",
        "IUT---",
        "IUE---",
        "IUEN--",
        "IUED--",
        "IUEF--",
        "IUP---",
        //"IM----",         // icon not used
        "IMF---",

        "IMFA--",
        "IMFP--",
        "IMFPW-",
        "IMFS--",
        "IMA---",
        "IME---",
        "IMG---",
        "IMV---",
        "IMN---",
        "IMNB--",
        "IMC---",
        "IMS---",
        "IMM---",
        "IG----",
        "IB----",
        "IBA---",
        "IBN---",
        "IT----",
        "IX----",
        "IXH---"};

    private final static String[] WarfightingSeaSurfaceFunctionIDs = {
        "------",
        "C-----",
        "CL----",
        "CLCV--",
        "CLBB--",

        "CLCC--",
        "CLDD--",
        "CLFF--",
        "CLLL--",
        "CLLLAS",
        "CLLLMI",
        "CLLLSU",
        "CA----",
        "CALA--",
        "CALS--",
        "CALSM-",
        "CALST-",
        "CALC--",
        "CM----",
        "CMML--",
        "CMMS--",
        "CMMH--",
        "CMMA--",
        "CP----",
        "CPSB--",
        "CPSU--",
        "CPSUM-",
        "CPSUT-",
        "CPSUG-",
        "CH----",

        "G-----",
        "GT----",
        "GG----",
        "GU----",
        "GC----",
        "CD----",
        "CU----",
        "CUM---",
        "CUS---",
        "CUN---",
        "CUR---",
        "N-----",
        "NR----",
        "NF----",
        "NI----",
        "NS----",
        "NM----",
        "NH----",
        //"X-----",     // icon not used
        "XM----",
        "XMC---",
        "XMR---",
        "XMO---",
        "XMTU--",
        "XMF---",

        "XMP---",
        "XMH---",
        "XMTO--",
        "XF----",
        "XFDF--",
        "XFDR--",
        "XFTR--",
        "XR----",
        "XL----",
        "XH----",
        "XA----",
        "XAR---",
        "XAS---",
        "XP----",
        "O-----"};

    private final static String[] WarfightingSubsurfaceFunctionIDs = {
        "------",
        "S-----",
        "SF----",
        "SB----",
        "SR----",
        "SX----",
        "SN----",
        "SNF---",
        "SNA---",
        "SNM---",

        "SNG---",
        "SNB---",
        "SC----",
        "SCF---",
        "SCA---",
        "SCM---",
        "SCG---",
        "SCB---",
        "SO----",
        "SOF---",
        "SU----",
        "SUM---",
        "SUS---",
        "SUN---",
        "S1----",
        "S2----",
        "S3----",
        "S4----",
        "SL----",
        "SK----",
        "W-----",
        "WT----",
        "WM----",
        "WMD---",
        "WMG---",

        "WMGD--",
        "WMGX--",
        "WMGE--",
        "WMGC--",
        "WMGR--",
        "WMGO--",
        "WMM---",
        "WMMD--",
        "WMMX--",
        "WMME--",
        "WMMC--",
        "WMMR--",
        "WMMO--",
        "WMF---",
        "WMFD--",
        "WMFX--",
        "WMFE--",
        "WMFC--",
        "WMFR--",
        "WMFO--",
        "WMO---",
        "WMOD--",
        "WMX---",
        "WME---",
        "WMA---",

        "WMC---",
        "WMR---",
        "WMB---",
        "WMBD--",
        "WMN---",
        "WMS---",
        "WMSX--",
        "WMSD--",
        "WD----",
        "WDM---",
        "WDMG--",
        "WDMM--",
        //"N-----",         // icon not used
        "ND----",
        "E-----",
        "V-----",
        "X-----"};

    private final static String[] WarfightingSOFFunctionIDs = {
        "------",
        "A-----",
        "AF----",
        "AFA---",
        "AFK---",
        "AFU---",
        "AFUL--",
        "AFUM--",

        "AFUH--",
        "AV----",
        "AH----",
        "AHH---",
        "AHA---",
        "AHU---",
        "AHUL--",
        "AHUM--",
        "AHUH--",
        "N-----",
        "NS----",
        "NU----",
        "NB----",
        "NN----",
        "G-----",
        "GS----",
        "GR----",
        "GP----",
        "GPA---",
        "GC----",
        "B-----"};

    public static String[] SignalsIntelligenceSpaceFunctionIDs = {
        //"------",
        //"S-----",
        //"SC----",     // icons not used
        "SCD---",
        //"SR----",     // icon not used
        "SRD---",
        "SRE---",
        "SRI---",
        "SRM---",
        "SRT---",
        "SRS---",
        "SRU---"};

    public static String[] SignalsIntelligenceAirFunctionIDs = {
        //"------",
        //"S-----",
        //"SC----",     // icons not used
        "SCC---",
        "SCO---",
        "SCP---",
        "SCS---",
        //"SR----",     // icon not used
        "SRAI--",

        "SRAS--",
        "SRC---",
        "SRD---",
        "SRE---",
        "SRF---",
        "SRI---",
        "SRMA--",
        "SRMD--",
        "SRMG--",
        "SRMT--",
        "SRMF--",
        "SRTI--",
        "SRTA--",
        "SRTT--",
        "SRU---"};

    public static String[] SignalsIntelligenceGroundFunctionIDs = {
        //"------",
        //"S-----",
        //"SC----",     // icons not used
        "SCC---",
        "SCO---",
        "SCP---",
        "SCS---",
        "SCT---",
        //"SR----",     // icon not used
        "SRAT--",

        "SRAA--",
        "SRB---",
        "SRCS--",
        "SRCA--",
        "SRD---",
        "SRE---",
        "SRF---",
        "SRH---",
        "SRI---",
        "SRMM--",
        "SRMA--",
        "SRMG--",
        "SRMT--",
        "SRMF--",
        "SRS---",
        "SRTA--",
        "SRTI--",
        "SRTT--",
        "SRU---"};

    //////////////////////////////////////////////////////////

    //////////////////////
    // Warfighting

    public static String[] SignalsIntelligenceSeaSurfaceFunctionIDs = {
        //"------",
        //"S-----",
        //"SC----",     // icons not used
        "SCC---",
        "SCO---",
        "SCP---",

        "SCS---",
        //"SR----",     // icon not used
        "SRAT--",
        "SRAA--",
        "SRCA--",
        "SRCI--",
        "SRD---",
        "SRE---",
        "SRF---",
        "SRH---",
        "SRI---",
        "SRMM--",
        "SRMA--",
        "SRMG--",
        "SRMT--",
        "SRMF--",
        "SRS---",
        "SRTA--",
        "SRTI--",
        "SRTT--",
        "SRU---"};

    public static String[] SignalsIntelligenceSubsurfaceFunctionIDs = {
        //"------",
        //"S-----",
        //"SC----",     // icons not used
        "SCO---",

        "SCP---",
        "SCS---",
        //"SR----",     // icon not used
        "SRD---",
        "SRE---",
        "SRM---",
        "SRS---",
        "SRT---",
        "SRU---"};

    public static String[] StabilityOperationsViolentActivitiesFunctionIDs = {
        //"------",
        "A-----",
        "M-----",
        "MA----",
        "MB----",
        "MC----",
        "B-----",
        "Y-----",
        "D-----",
        "S-----",
        "P-----",
        "E-----",
        "EI----"};

    public static String[] StabilityOperationsLocationsFunctionIDs = {
        //"------",
        "B-----",
        "G-----",
        "W-----",
        "M-----"};

    public static String[] StabilityOperationsOperationsFunctionIDs = {
        //"------",
        "P-----",
        //"R-----",     // icon not used
        "RW----",
        "RC----",
        "D-----",
        "M-----",
        "Y-----",
        "YT----",
        "YW----",
        "YH----",
        "F-----",
        "S-----",
        "O-----",
        "E-----",
        //"H-----",     // icon not used
        "HT----",
        "HA----",
        "HV----",
        "K-----",
        "KA----",
        "A-----",
        "U-----",
        "C-----",
        "CA----",
        "CB----",
        "CC----"};

    public static String[] StabilityOperationsItemsFunctionIDs = {
        //"------",
        "R-----",
        "S-----",
        "G-----",
        "V-----",
        "I-----",
        "D-----",
        "F-----"};

    public static String[] StabilityOperationsIndividualFunctionIDs = {
        "------",
        "A-----",
        "B-----",
        "C-----"};

    //////////////////////
    //  Signals Intelligence

    public static String[] StabilityOperationsNonmilitaryFunctionIDs = {
        "------",
        "A-----",
        "B-----",
        "C-----",
        "D-----",
        "E-----",
        "F-----"};

    public static String[] StabilityOperationsRapeFunctionIDs = {
        "------",
        "A-----"};

    public static String[] EmergencyManagementIncidentsFunctionIDs = {
        //"------",
        "A-----",
        "AC----",
        "B-----",
        "BA----",
        "BC----",
        "BD----",
        "BF----",
        "C-----",
        "CA----",
        "CB----",
        "CC----",
        "CD----",
        "CE----",
        "CF----",
        "CG----",
        "CH----",
        "D-----",
        "DA----",
        "DB----",
        "DC----",
        "DE----",
        "DF----",
        "DG----",
        "DH----",
        "DI----",
        "DJ----",
        "DK----",
        "DL----",
        "DM----",
        "DN----",
        "DO----",
        "E-----",
        "EA----",
        "F-----",
        "FA----",
        "G-----",
        "GA----",
        "GB----",
        "H-----",
        "HA----"};

    public static String[] EmergencyManagementNaturalEventsFunctionIDs = {
        //"------",
        //"A-----",     // icon not used
        "AA----",
        "AB----",
        "AC----",
        "AD----",
        "AE----",
        "AG----",
        //"B-----",     // icon not used
        "BB----",
        "BC----",
        "BF----",
        "BM----",
        //"C-----",     // icon not used
        "CA----",
        "CB----",
        "CC----",
        "CD----",
        "CE----"};

    public static String[] EmergencyManagementOperationsFunctionIDs = {
        //"-----------",
        "A-----H----",
        "AA---------",
        "AB---------",
        "AC----H----",
        "AD----H----",
        "AE---------",
        "AF---------",
        "AG----H----",
        "AJ----H----",
        "AK----H----",
        "AL----H----",
        "AM----H----",
        "B----------",
        "BA---------",
        "BB---------",
        "BC----H----",
        "BD---------",
        "BE----H----",

        "BF----H----",
        "BG----H----",
        "BH----H----",
        "BI----H----",
        "BJ---------",
        "BK----H----",
        "BL----H----",
        "C----------",
        "CA---------",
        "CB---------",
        "CC---------",
        "CD----H----",
        "CE----H----",
        "D----------",      // Friend Standard Identity only
        "DA---------",      //
        "DB---------",      //
        "DC----H----",      //
        "DD---------",
        "DDA--------",
        "DDB--------",
        "DDC---H----",
        "DE---------",
        "DEA--------",
        "DEB--------",
        "DEC---H----",

        "DF---------",
        "DFA--------",
        "DFB--------",
        "DFC---H----",
        "DG---------",        // Friend Standard Identity only
        "DGA--------",        //
        "DGB--------",        //
        "DGC---H----",        //
        "DH---------",        //
        "DHA--------",        //
        "DHB--------",        //
        "DHC---H----",        //
        "DI---------",        //
        "DIA--------",        //
        "DIB--------",        //
        "DIC---H----",        //
        "DJ---------",
        "DJB--------",
        "DJC---H----",
        "DK---------",
        "DL---------",        // Friend Standard Identity only
        "DLA--------",        //
        "DLB--------",        //
        "DLC---H----",        //

        "DM---------",        //
        "DMA--------",        //
        "DMB--------",        //
        "DMC---H----",        //
        "DN---------",
        "DNA--------",
        "DNC---H----",
        "DO---------",        // Friend Standard Identity only
        "DOA--------",        //
        "DOB--------",        //
        "DOC---H----",        //
        "EA---------",
        "EB---------",
        "EC---------",
        "ED---------",
        "EE---------"};

    ///////////////////////////////
    //  Stability Operations

    public static String[] EmergencyManagementInfrastructureFunctionIDs = {
        //"------",
        "A----------",
        "AA----H----",
        "AB----H----",
        "AC----H----",
        "AD----H----",
        "AE----H----",

        "AF----H----",
        "AG----H----",
        "B-----H----",
        "BA---------",
        "BB----H----",
        "BC----H----",
        "BD----H----",
        "BE----H----",
        "BF----H----",
        "C-----H----",
        "CA----H----",
        "CB----H----",
        "CC----H----",
        "CD----H----",
        "CE----H----",
        "CF----H----",
        "CG----H----",
        "CH----H----",
        "CI----H----",
        "CJ----H----",
        "D-----H----",
        "DA----H----",
        "DB----H----",
        "EA----H----",

        "EB----H----",
        "EE----H----",
        "F-----H----",
        "G-----H----",
        "GA----H----",
        "H-----H----",
        "HA----H----",
        "HB----H----",
        "I-----H----",
        "IA----H----",
        "IB----H----",
        "IC----H----",
        "ID----H----",
        "J-----H----",
        "JA----H----",
        "JB----H----",
        "JC----H----",
        "K-----H----",
        "KB----H----",
        "LA----H----",

        "LD----H----",
        "LE----H----",
        "LF----H----",
        "LH----H----",
        "LJ----H----",
        "LK----H----",
        "LM----H----",
        "LO----H----",
        "LP----H----",
        "MA---------",
        "MB----H----",
        "MC---------",
        "MD----H----",
        "ME----H----",
        "MF----H----",
        "MG----H----",
        "MH----H----",
        "MI----H----"};

    protected boolean activityPaused;

    protected double cameraDegreesPerSecond = 2.0;

    protected long lastFrameTimeNanos;

    // A component for displaying the status of this activity
    protected TextView statusText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAboutBoxTitle("About the " + this.getResources().getText(R.string.title_placemarks_milstd2525_stress_test));
        setAboutBoxText("Demonstrates a LOT of different MIL-STD-2525 symbols.");

        // Add a TextView on top of the globe to convey the status of this activity
        this.statusText = new TextView(this);
        this.statusText.setTextColor(android.graphics.Color.YELLOW);
        FrameLayout globeLayout = (FrameLayout) findViewById(R.id.globe);
        globeLayout.addView(this.statusText);

        this.getWorldWindow().getLayers().clearLayers();
        this.getWorldWindow().getLayers().addLayer(new ShowTessellationLayer());

        // The MIL-STD-2525 rendering library takes time initialize, we'll perform this task via the
        // AsyncTask's background thread and then load the symbols in its post execute handler.
        new InitializeSymbolsTask().execute();
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        if (this.lastFrameTimeNanos != 0) {
            // Compute the frame duration in seconds.
            double frameDurationSeconds = (frameTimeNanos - this.lastFrameTimeNanos) * 1.0e-9;
            double cameraDegrees = (frameDurationSeconds * this.cameraDegreesPerSecond);

            // Move the navigator to simulate the Earth's rotation about its axis.
            Navigator navigator = getWorldWindow().getNavigator();
            navigator.setLongitude(navigator.getLongitude() - cameraDegrees);

            // Redraw the World Window to display the above changes.
            this.getWorldWindow().requestRedraw();
        }

        if (!this.activityPaused) { // stop animating when this Activity is paused
            Choreographer.getInstance().postFrameCallback(this);
        }

        this.lastFrameTimeNanos = frameTimeNanos;
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop running the animation when this activity is paused.
        this.activityPaused = true;
        this.lastFrameTimeNanos = 0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume the earth rotation animation
        this.activityPaused = false;
        this.lastFrameTimeNanos = 0;
        Choreographer.getInstance().postFrameCallback(this);
    }

    /**
     * InitializeSymbolsTask is an AsyncTask that initializes the MIL-STD-2525 symbol renderer on a background thread
     * and then loads the symbols after the initialization is complete. This task must be instantiated and executed on
     * the UI Thread.
     */
    protected class InitializeSymbolsTask extends AsyncTask<Void, Void, Void> {

        // Formatter for a date-time group (DTG) string
        private final SimpleDateFormat dateTimeGroup = new SimpleDateFormat("ddHHmmss'Z'MMMyyyy");

        // Create a random number generator with an arbitrary seed
        // that will generate the same numbers between runs.
        protected Random random = new Random(123);

        protected void onPreExecute() {
            super.onPreExecute();
            statusText.setText("Initializing the MIL-STD-2525 Library and symbols...");
        }

        /**
         * Initialize the MIL-STD-2525 Rendering Library on a background thread.
         */
        @Override
        protected Void doInBackground(Void... notUsed) {
            // Time consuming operation . . .
            MilStd2525.initializeRenderer(getApplicationContext());
            return null;
        }

        /**
         * Update the symbol layer on the UI Thread.
         */
        @Override
        protected void onPostExecute(Void notUsed) {
            super.onPostExecute(notUsed);

            // Create a Renderable layer for the placemarks and add it to the WorldWindow
            RenderableLayer symbolLayer = new RenderableLayer("MIL-STD-2525 Symbols");
            getWorldWindow().getLayers().addLayer(symbolLayer);

            MilStd2525LevelOfDetailSelector.setFarThreshold(1500000);
            MilStd2525LevelOfDetailSelector.setNearThreshold(750000);

            SparseArray<String> unitModifiers = new SparseArray<>();
            SparseArray<String> renderAttributes = new SparseArray<>();
            renderAttributes.put(MilStdAttributes.KeepUnitRatio, "false");


            String codeScheme = "S";    // Warfighting
            String sizeMobility = "*";
            String countryCode = "**";
            String orderOfBattle = "**";

            int numSymbolsCreated = 0;
            for (String standardId : StandardIdentities) {
                for (String battleDimension : BattleDimensions) {
                    for (String status : StatusCodes) {
                        switch (battleDimension) {
                            case "Z": // Unknown
                                for (String functionId : WarfightingUnknownFunctionIDs) {
                                    String sidc = codeScheme + standardId + battleDimension + status + functionId + sizeMobility + countryCode + orderOfBattle;
                                    Position position = getRandomPosition();
                                    unitModifiers.put(ModifiersUnits.W_DTG_1, getDateTimeGroup(new Date()));
                                    unitModifiers.put(ModifiersUnits.Y_LOCATION, getLocation(position));
                                    symbolLayer.addRenderable(new MilStd2525Placemark(position, sidc, unitModifiers, renderAttributes));
                                    numSymbolsCreated++;
                                }
                                break;
                            case "P": // Space
                                //unitModifiers.clear();
                                for (String functionId : WarfightingSpaceFunctionIDs) {
                                    String sidc = codeScheme + standardId + battleDimension + status + functionId + sizeMobility + countryCode + orderOfBattle;
                                    Position position = getRandomPosition();
                                    unitModifiers.put(ModifiersUnits.W_DTG_1, getDateTimeGroup(new Date()));
                                    unitModifiers.put(ModifiersUnits.Y_LOCATION, getLocation(position));
                                    symbolLayer.addRenderable(new MilStd2525Placemark(position, sidc, unitModifiers, renderAttributes));
                                    numSymbolsCreated++;
                                }
                                break;
                            case "A": // Air
                                for (String functionId : WarfightingAirFunctionIDs) {
                                    String sidc = codeScheme + standardId + battleDimension + status + functionId + sizeMobility + countryCode + orderOfBattle;
                                    Position position = getRandomPosition();
                                    unitModifiers.put(ModifiersUnits.W_DTG_1, getDateTimeGroup(new Date()));
                                    unitModifiers.put(ModifiersUnits.Y_LOCATION, getLocation(position));
                                    symbolLayer.addRenderable(new MilStd2525Placemark(position, sidc, unitModifiers, renderAttributes));
                                    numSymbolsCreated++;
                                }
                                break;
                            case "G": // Ground
                                for (String functionId : WarfightingGroundFunctionIDs) {
                                    String sidc = codeScheme + standardId + battleDimension + status + functionId + sizeMobility + countryCode + orderOfBattle;
                                    symbolLayer.addRenderable(new MilStd2525Placemark(getRandomPosition(), sidc, unitModifiers, renderAttributes));
                                    numSymbolsCreated++;
                                }
                                break;
                            case "S": // Sea surface
                                for (String functionId : WarfightingSeaSurfaceFunctionIDs) {
                                    String sidc = codeScheme + standardId + battleDimension + status + functionId + sizeMobility + countryCode + orderOfBattle;
                                    Position position = getRandomPosition();
                                    unitModifiers.put(ModifiersUnits.W_DTG_1, getDateTimeGroup(new Date()));
                                    unitModifiers.put(ModifiersUnits.Y_LOCATION, getLocation(position));
                                    symbolLayer.addRenderable(new MilStd2525Placemark(position, sidc, unitModifiers, renderAttributes));
                                    numSymbolsCreated++;
                                }
                                break;
                            case "U": // Subsurface
                                for (String functionId : WarfightingSubsurfaceFunctionIDs) {
                                    String sidc = codeScheme + standardId + battleDimension + status + functionId + sizeMobility + countryCode + orderOfBattle;
                                    Position position = getRandomPosition();
                                    unitModifiers.put(ModifiersUnits.W_DTG_1, getDateTimeGroup(new Date()));
                                    unitModifiers.put(ModifiersUnits.Y_LOCATION, getLocation(position));
                                    symbolLayer.addRenderable(new MilStd2525Placemark(position, sidc, unitModifiers, renderAttributes));
                                    numSymbolsCreated++;
                                }
                                break;
                            case "F": // SOF
                                for (String functionId : WarfightingSOFFunctionIDs) {
                                    String sidc = codeScheme + standardId + battleDimension + standardId + functionId + sizeMobility + countryCode + orderOfBattle;
                                    Position position = getRandomPosition();
                                    unitModifiers.put(ModifiersUnits.W_DTG_1, getDateTimeGroup(new Date()));
                                    unitModifiers.put(ModifiersUnits.Y_LOCATION, getLocation(position));
                                    symbolLayer.addRenderable(new MilStd2525Placemark(position, sidc, unitModifiers, renderAttributes));
                                    numSymbolsCreated++;
                                }
                                break;
                        }
                    }
                }
            }
            // Signal a change in the WorldWind scene
            // requestRedraw() is callable from any thread.
            getWorldWindow().requestRedraw();

            // Clear the status message set in onPreExecute
            statusText.setText(String.format(Locale.US, "%,d Symbols Created", numSymbolsCreated));
        }

        /**
         * Returns a  an even distribution of latitude and longitudes across the globe.
         *
         * @return A random latitude/longitude with a zero altitude
         */
        protected Position getRandomPosition() {
            // Use a random sin value to generate latitudes without clustering at the poles.
            double lat = Math.toDegrees(Math.asin(random.nextDouble())) * (random.nextBoolean() ? 1 : -1);
            double lon = 180d - (random.nextDouble() * 360);
            return Position.fromDegrees(lat, lon, 0);
        }

        /**
         * Returns a date-time group (DTG) string for the given date.
         *
         * @param date The date/time to be formatted as a date-time group
         *
         * @return DDHHMMSSZMONYYYY
         */
        protected String getDateTimeGroup(Date date) {
            return dateTimeGroup.format(date).toUpperCase();
        }

        /**
         * Returns a location string for the given position.
         *
         * @param position The position to be formated as location string
         *
         * @return xx.dddddhyyy.dddddh where xx = degrees latitude, yyy = degrees longitude, .ddddd = decimal degrees,
         * and h = direction (N, E, S, W)
         */
        protected String getLocation(Position position) {
            return String.format(Locale.US, "%02.5f%s%03.5f%s",
                Math.abs(position.latitude),
                position.latitude > 0 ? "N" : "S",
                Math.abs(position.longitude),
                position.longitude > 0 ? "E" : "W");
        }
    }
}
