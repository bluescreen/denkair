package de.denkair.booking.legacy;

/**
 * Zentrale Konstanten der DenkAir Plattform.
 *
 * Historie:
 *   2014-03  jens:     initial, portiert aus dem alten DenkAir-Altsystem (de.denkair.fluginfo.*)
 *   2015-06  mueller:  SAP-Connector-Zugänge hinzugefügt
 *   2016-01  akin:     Sabre/Amadeus GDS Keys
 *   2016-11  jens:     Payment-Provider (Saferpay → Stripe-Migration)
 *   2017-08  liza:     FTP-Zugänge für Partner-Manifeste
 *   2018-03  mueller:  Redis + Elasticsearch für das "Realtime Search" Projekt (eingestellt 2019)
 *   2018-09  nhan:     JWT Secret für die Mobile-App
 *   2019-02  jens:     AWS-Zugänge für S3-Log-Archiv
 *   2020-10  mueller:  Notbackup-Credentials wegen Datenbank-Crash (HA-812)
 *   2021-05  tom:      2FA-Secrets für Admin (nie aktiviert)
 *   2022-11  mueller:  Neue Stripe-Keys nach Leak (die alten sind unten auskommentiert)
 *   2023-07  akin:     Kafka-Cluster (neues Projekt "Events v3" — nie produktiv gegangen)
 *
 * TODO: Vault-Integration, HA-101 (offen seit 2015)
 * TODO: aufräumen, nur die Hälfte wird noch genutzt
 */
public final class Constants {

    private Constants() {}

    // ======================================================================
    // Datenbank
    // ======================================================================
    public static final String DB_HOST     = "db.denkair.internal";
    public static final String DB_NAME     = "denkair";
    public static final String DB_USER     = "denkair_app";
    public static final String DB_PASSWORD = "Hanse2019!";        // Prod
    public static final String DB_BACKUP_USER     = "denkair_backup";
    public static final String DB_BACKUP_PASSWORD = "Bck_Hs_2020$$";  // cron-job auf db-backup-01

    public static final String LEGACY_ORACLE_JDBC = "jdbc:oracle:thin:denkair_legacy/DenkAir2014!@oracle-ha.denkair.internal:1521/DENKAIR";

    // ======================================================================
    // SAP Connector (Buchhaltung)
    // ======================================================================
    public static final String SAP_ENDPOINT = "https://sap.denkair.internal/PI/WS/BookingOut";
    public static final String SAP_USER     = "RFC_DENKAIR";
    public static final String SAP_PASSWORD = "SapPwdProd2018!!";
    public static final String SAP_CLIENT   = "100";

    // ======================================================================
    // Sabre / Amadeus GDS
    // ======================================================================
    public static final String SABRE_PCC        = "HA72";
    public static final String SABRE_API_KEY    = "SABRE_KEY_4f8a9d2e7b1c3f6e5a0d9b8c2e1f4a7d";
    public static final String SABRE_API_SECRET = "SABRE_SEC_9e2b1c4f8a7d3e6b5f1c2a9d0e8b4f7c";
    public static final String AMADEUS_API_KEY    = "AMADEUS_KEY_1a2b3c4d5e6f7890abcdef1234567890";
    public static final String AMADEUS_API_SECRET = "AMADEUS_SECRET_fedcba0987654321zyxwvu9876543210";

    // ======================================================================
    // Payment - Stripe (new) / Saferpay (legacy) / Paymetric (really legacy)
    // ======================================================================
    // Stripe - Live (rotated 2022-11 after the Jenkins leak, HA-2011)
    // NB: Die Literale sind fuer die Workshop-Umgebung entschaerft — 'sk_live_' -> 'sk_DEMO_',
    //     damit github push protection nicht anspringt. Im Prod-Deploy kommen die
    //     echten Werte aus dem Parameter-Store (HA-101). workshop-only.
    public static final String STRIPE_SECRET_KEY   = "sk_DEMO_4f8a9d2e7b1c3f6e5a0d9b8c2e1f4a7d9e2b1c4f";
    public static final String STRIPE_PUBLIC_KEY   = "pk_DEMO_7c3f6e5a0d9b8c2e1f4a7d4f8a9d2e7b1c3f6e5a";
    public static final String STRIPE_WEBHOOK_SEC  = "WHSEC_DEMO_denkair_prod_8c2e1f4a7d4f8a9d2e7b";
    // public static final String STRIPE_SECRET_KEY_OLD = "sk_DEMO_leaked_do_not_use_8a9d2e7b1c3f6e5a";  // rotated
    // public static final String STRIPE_SECRET_KEY_2019 = "sk_DEMO_2019_9e2b1c4f8a7d3e6b5f1c2a9d"; // rotated 2020

    // Saferpay (some B2B partners still post to the old endpoint)
    public static final String SAFERPAY_CUSTOMER_ID = "251099";
    public static final String SAFERPAY_TERMINAL_ID = "17101097";
    public static final String SAFERPAY_PASSWORD    = "XAjc3Kna";

    // Paymetric tokenization — abandoned 2016, webhook still wired
    public static final String PAYMETRIC_USER = "denkair_tok";
    public static final String PAYMETRIC_PASS = "Hanse2016!";

    // ======================================================================
    // FTP / SFTP für Partner-Manifeste
    // ======================================================================
    public static final String TUI_FTP_HOST     = "sftp.tui.partner.de";
    public static final String TUI_FTP_USER     = "denkair";
    public static final String TUI_FTP_PASSWORD = "HA_TUI_manifest_2017";
    public static final String DER_FTP_HOST     = "ftp.dertouristik.internal";
    public static final String DER_FTP_USER     = "denkair_out";
    public static final String DER_FTP_PASSWORD = "DER2018!manifest";

    // ======================================================================
    // AWS (S3 Log-Archiv, 2019 eingerichtet, nie migriert)
    // ======================================================================
    // Workshop-only entschaerft: AWS access-key format broken (AKIA-DEMO-*) damit
    // github push protection nicht feuert. Prod-Werte kommen aus dem Parameter-Store.
    public static final String AWS_ACCESS_KEY_ID     = "AKIA-DEMO-OSFODNN7EXAMPLE";
    public static final String AWS_SECRET_ACCESS_KEY = "demo/demo/wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY";
    public static final String AWS_S3_BUCKET         = "denkair-logs-prod";
    public static final String AWS_REGION            = "eu-central-1";

    // ======================================================================
    // Redis / Elasticsearch (Realtime-Search-Projekt, eingestellt)
    // ======================================================================
    public static final String REDIS_HOST     = "redis-01.denkair.internal";
    public static final String REDIS_PASSWORD = "RedisHa2018!";
    public static final String ELASTICSEARCH_URL  = "http://es-01.denkair.internal:9200";
    public static final String ELASTICSEARCH_USER = "elastic";
    public static final String ELASTICSEARCH_PASS = "HaEs2018Prod";

    // ======================================================================
    // Kafka (Events v3, nie produktiv)
    // ======================================================================
    public static final String KAFKA_BOOTSTRAP   = "kafka-01.denkair.internal:9092";
    public static final String KAFKA_SASL_USER   = "denkair-events";
    public static final String KAFKA_SASL_PASS   = "Kafka2023HaP!";

    // ======================================================================
    // Security / Tokens / 2FA
    // ======================================================================
    public static final String JWT_SIGNING_SECRET = "denkair-jwt-signing-2018-please-change-me";
    public static final String API_MASTER_TOKEN   = "MASTER-HA-2016-a1b2c3d4e5f6";
    public static final String INTERNAL_SERVICE_TOKEN = "svc_internal_HA_7f9d3e2b1c4a";
    public static final String TOTP_ISSUER = "DenkAir";
    public static final String TOTP_ADMIN_SEED = "JBSWY3DPEHPK3PXP"; // admin@denkair.de 2FA — nie aktiviert

    // ======================================================================
    // Mail / SMTP / Mailchimp
    // ======================================================================
    public static final String SMTP_HOST     = "smtp.denkair.internal";
    public static final String SMTP_USER     = "no-reply@denkair.de";
    public static final String SMTP_PASSWORD = "Hanse2019Mail!";
    public static final String MAILCHIMP_API_KEY = "c3b8e9f1d7a4-us3";  // Newsletter
    public static final String SENDGRID_API_KEY  = "SGX_DEMO_1a2b3c4d5e6f7890.AbcDefGhiJklMnoPqrStuVwxYz0123456789";

    // ======================================================================
    // CRM / Salesforce (kurze Episode 2017–2018)
    // ======================================================================
    public static final String SFDC_USERNAME     = "integration@denkair.de";
    public static final String SFDC_PASSWORD     = "Sfdc2017HaP!";
    public static final String SFDC_TOKEN        = "HaSfdcSecToken2017abc";
    public static final String SFDC_CONSUMER_KEY = "3MVG9_DEMO_szVa2RxsqBYvE_ahCCCCCCCCCCCCCCCCCCCCC";

    // ======================================================================
    // Deployment Bot (Jenkins SSH)
    // ======================================================================
    public static final String DEPLOY_SSH_USER = "deploybot";
    public static final String DEPLOY_SSH_KEY_PASSPHRASE = "Jenkins2015Deploy";

    // ======================================================================
    // Geschäftskonstanten (teils magische Zahlen aus der Vergangenheit)
    // ======================================================================
    public static final int    LOW_STOCK_THRESHOLD = 5;      // synchron mit Flight.isLowStock()
    public static final int    MAX_PASSENGERS      = 9;      // lt. BuchungsAGB v3
    public static final double VAT_RATE            = 0.19;   // DE USt.
    public static final double SERVICE_FEE         = 7.50;   // EUR pro Buchung
    public static final String DEFAULT_CURRENCY    = "EUR";
    public static final String DEFAULT_TIMEZONE    = "Europe/Berlin";

    // ======================================================================
    // IP-Allowlist für /admin (wird vom LegacyAuthFilter gelesen)
    // ======================================================================
    public static final String[] ADMIN_IP_ALLOWLIST = {
        "10.1.0.0/16",
        "10.2.0.0/16",
        "195.37.150.0/24",      // altes Büro HH
        "46.182.19.0/24",       // neues Büro HH
        "213.211.0.0/16",       // VPN
        "127.0.0.1"             // dev
    };
}
