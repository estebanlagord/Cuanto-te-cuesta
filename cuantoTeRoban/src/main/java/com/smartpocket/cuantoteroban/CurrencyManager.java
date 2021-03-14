package com.smartpocket.cuantoteroban;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

public class CurrencyManager {
    public static final String USD = "USD";
    public static final String BRL = "BRL";
    public static final String EUR = "EUR";
    public static final String UYU = "UYU";
    public static final String ARS = "ARS";

    private static CurrencyManager instance;
    private final Set<Currency> allCurrencies = new TreeSet<>();

    public CurrencyManager() {
        buildAllCurrencies();
    }

/*    @Deprecated
	public static CurrencyManager getInstance() {
		if (instance == null)
			instance = new CurrencyManager();

		return instance;
	}*/

    public Set<Currency> getAllCurrencies() {
        return allCurrencies;
    }

    public Currency findCurrency(String code) {
        Currency result = null;

        for (Currency curr : allCurrencies) {
            if (curr.getCode().equalsIgnoreCase(code)) {
                result = curr;
                break;
            }
        }
        if (result == null)
            throw new IllegalArgumentException("The code " + code + " is not a valid currency");

        return result;
    }

    private void buildAllCurrencies() {
        allCurrencies.clear();

        allCurrencies.addAll(Arrays.asList(

                new Currency("THB", "Baht tailandés", "Tailandia", R.drawable.thb),
                new Currency("ETB", "Birr etíope", "Etiopia", R.drawable.etb),
                new Currency("VES", "Bolívar", "Venezuela", R.drawable.vef),
                new Currency("BOB", "Boliviano", "Bolivia", R.drawable.bob),
                new Currency("PAB", "Balboa panameño", "Panamá", R.drawable.pab),
                new Currency("CRC", "Colón costarricense", "Costa Rica", R.drawable.crc),
                new Currency("DKK", "Corona danesa", "Dinamarca", R.drawable.dkk),
                //new Currency("EEK", "Corona estonia", "Estonia", R.drawable.eek),
                new Currency("ISK", "Corona islandesa", "Islandia", R.drawable.isk),
                new Currency("NIO", "Córdoba nicaragüense", "Nicaragua", R.drawable.nio),
                //new Currency("SKK", "Corona eslovaca", "Eslovaquia", R.drawable.skk),
                new Currency("SEK", "Corona sueca", "Suecia", R.drawable.sek),
                new Currency("UGX", "Chelín ugandés", "Uganda", R.drawable.ugx),
                new Currency("CZK", "Corona checa", "Republica Checa", R.drawable.czk),
                new Currency("SVC", "Colón de El Salvador", "El Salvador", R.drawable.svc),
                //new Currency("GHC", "Cedi ghanés", "Ghana", R.drawable.ghc),
                new Currency("KES", "Chelín keniano", "Kenia", R.drawable.kes),
                new Currency("NOK", "Corona noruega", "Noruega", R.drawable.nok),
                new Currency("SOS", "Chelín somalí", "Somalia", R.drawable.sos),
                new Currency("TZS", "Chelín tanzano", "Tanzania", R.drawable.tzs),
                new Currency(EUR, "Euro", "Union Europea", R.drawable.eur),
                new Currency("AUD", "Dólar australiano", "Australia", R.drawable.aud),
                new Currency("CHF", "Franco suizo", "Suiza", R.drawable.chf),
                new Currency("DZD", "Dinar argelino", "Argelia", R.drawable.dzd),
                new Currency("BSD", "Dólar bahameño", "Bahamas", R.drawable.bsd),
                new Currency("BBD", "Dólar de Barbados", "Barbados", R.drawable.bbd),
                new Currency("BMD", "Dólar bermudeño", "Bermudas", R.drawable.bmd),
                new Currency("BIF", "Franco de Burundi", "Burundi", R.drawable.bif),
                //new Currency("KYD", "Dólar de las Islas Caimán", "Islas Caimán", R.drawable.kyd),
                new Currency("XAF", "Franco CFA (BEAC)", "África", "Benín Burkina Faso Costa de Marfil Guinea Bissau Mali Níger Senegal Togo", R.drawable.xaf),
                new Currency("DJF", "Franco yibutiano", "Yibuti", R.drawable.djf),
                new Currency("FJD", "Dólar fiyiano", "Fiyi", R.drawable.fjd),
                new Currency("GNF", "Franco guineano", "Guinea", R.drawable.gnf),
                new Currency("HUF", "Florín húngaro", "Hungría", R.drawable.huf),
                new Currency("JMD", "Dólar jamaiquino", "Jamaica", R.drawable.jmd),
                new Currency("KWD", "Dinar kuwaití", "Kuwait", R.drawable.kwd),
                new Currency("LYD", "Dinar libio", "Libia", R.drawable.lyd),
                new Currency("MAD", "Dirham marroquí", "Marruecos", R.drawable.mad),
                new Currency("ANG", "Florín antillano neerlandés", "Curazao / Sint Maarten", "Curasao Curaçao", R.drawable.ang),
                new Currency("XPF", "Franco del Pacífico", " Polinesia Francesa / Nueva Caledonia / Wallis y Futuna", R.drawable.xpf),
                new Currency("STD", "Dobra de Santo Tomé", "Santo Tomé", R.drawable.std),
                new Currency("SBD", "Dólar de las Islas Salomón", "Islas Salomón", R.drawable.sbd),
                new Currency("TTD", "Dólar trinitense", "Trinidad y Tobago", R.drawable.ttd),
                new Currency("AED", "Dirham de los Emiratos Árabes Unidos", "Emiratos Árabes Unidos", R.drawable.aed),
                //new Currency("ZWD", "Dólar zimbabuense", "Zimbabue", R.drawable.zwd),
                new Currency(USD, "Dólar estadounidense", "Estados Unidos", "EEUU EE.UU. USA U.S.A", R.drawable.usd),
                new Currency("CAD", "Dólar canadiense", "Canada", R.drawable.cad),
                new Currency("HKD", "Dólar de Hong Kong", "Hong Kong", R.drawable.hkd),
                new Currency("AWG", "Florín arubeño", "Aruba", R.drawable.awg),
                new Currency("BHD", "Dinar bahreiní", "Baréin", R.drawable.bhd),
                new Currency("BZD", "Dólar beliceño", "Belice", "Belize", R.drawable.bzd),
                new Currency("BND", "Dólar de Brunéi", "Brunéi", R.drawable.bnd),
                new Currency("CVE", "Escudo caboverdiano", "Cabo Verde", R.drawable.cve),
                new Currency("XOF", "Franco CFA (BEAO)", "África", "Camerún Chad Gabón Guinea Ecuatorial República Centroafricana República del Congo", R.drawable.xof),
                new Currency("KMF", "Franco comorano", "Comoras", R.drawable.kmf),
                new Currency("XCD", "Dólar del Caribe Oriental", "Antigua y Barbuda / Dominica / Granada / San Cristóbal y Nieves / Santa Lucía / San Vicente y las Granadinas / Anguila / Montserrat", "Caribe", R.drawable.xcd),
                new Currency("GMD", "Dalasi gambiano", "Gambia", R.drawable.gmd),
                new Currency("GYD", "Dólar guyanés", "Guyana", R.drawable.gyd),
                new Currency("IQD", "Dinar iraquí", "Irak", R.drawable.iqd),
                new Currency("JOD", "Dinar jordano", "Jordania", R.drawable.jod),
                new Currency("LRD", "Dólar liberiano", "Liberia", R.drawable.lrd),
                new Currency("MKD", "Dinar macedonio", "Macedonia", R.drawable.mkd),
                new Currency("NAD", "Dólar namibio", "Namibia", R.drawable.nad),
                new Currency("NZD", "Dólar neozelandés", "Nueva Zelanda", R.drawable.nzd),
                new Currency("RWF", "Franco ruandés", "Ruanda", R.drawable.rwf),
                new Currency("SGD", "Dólar de Singapur", "Singapur", R.drawable.sgd),
                new Currency("TWD", "Dólar taiwanés", "Taiwan", R.drawable.twd),
                new Currency("TND", "Dinar tunecino", "Túnez", R.drawable.tnd),
                new Currency("VND", "Dong de Vietnam", "Vietnam", R.drawable.vnd),
                new Currency("HTG", "Gourde haitiano", "Haití", R.drawable.htg),
                new Currency("UAH", "Grivnia ucraniana", "Ucrania", R.drawable.uah),
                new Currency("PYG", "Guaraní paraguayo", "Paraguay", R.drawable.pyg),
                new Currency("GBP", "Libra esterlina", "Gran Bretaña", "Inglaterra", R.drawable.gbp),
                new Currency("BGN", "Lev búlgaro", "Bulgaria", R.drawable.bgn),
                new Currency("HRK", "Kuna croata", "Croacia", R.drawable.hrk),
                new Currency("FKP", "Libra malvinense", "Islas Malvinas", " Islas Georgias del Sur Sandwich del Sur", R.drawable.fkp),
                new Currency("HNL", "Lempira hondureña", "Honduras", R.drawable.hnl),
                new Currency("LVL", "Lats letón", "Letonia", R.drawable.lvl),
                new Currency("LSL", "Loti lesotense", "Lesoto", R.drawable.lsl),
                new Currency("MWK", "Kwacha malauí", "Malaui", R.drawable.mwk),
                new Currency("MDL", "Leu moldavo", "Moldavia", R.drawable.mdl),
                new Currency("TRY", "Lira turca", " Turquía / República Turca del Norte de Chipre", R.drawable.try_turquia),
                new Currency("RON", "Leu rumano", "Rumania", R.drawable.ron),
                new Currency("SHP", "Libra de Santa Helena", " Santa Elena /  Ascensión / Tristán de Acuña", R.drawable.shp),
                new Currency("SZL", "Lilangeni de Suazilandia", "Suazilandia", R.drawable.szl),
                new Currency("ZMK", "Kwacha zambiano", "Zambia", R.drawable.zmk),
                new Currency("ALL", "Lek albanés", "Albania", R.drawable.all),
                new Currency("EGP", "Libra egipcia", "Egipto", R.drawable.egp),
                new Currency("GIP", "Libra gibraltareña", "Gibraltar", R.drawable.gip),
                new Currency("LAK", "Kip laosiano", "Laos", R.drawable.lak),
                new Currency("LBP", "Libra libanesa", "Líbano", R.drawable.lbp),
                new Currency("LTL", "Litas lituana", "Lituania", R.drawable.ltl),
                //new Currency("MTL", "Lira maltesa", "Malta", R.drawable.mtl),
                new Currency("MMK", "Kyat birmano", "Birmania", "Unión de Myanmar", R.drawable.mmk),
                new Currency("PGK", "Kina de Papúa Nueva Guinea", "Papúa Nueva Guinea", R.drawable.pgk),
                new Currency("SLL", "Leone de Sierra Leona", "Sierra Leona", R.drawable.sll),
                new Currency("SDG", "Dinar sudanés", "Sudán", R.drawable.sdg),
                new Currency("SYP", "Libra siria", "Siria", R.drawable.syp),
                //new Currency("ERN", "Nakfa de Eritrea", "Eritrea", R.drawable.ern),
                new Currency("NGN", "Naira nigeriano", "Nigeria", R.drawable.ngn),
                new Currency("PEN", "Nuevo sol peruano", "Perú", R.drawable.pen),
                new Currency("BTN", "Ngultrum butanés", "Bután", R.drawable.btn),
                new Currency("IDR", "Rupia indonesia", "Indonesia", R.drawable.idr),
                new Currency("BWP", "Pula botsuana", "Botsuana", R.drawable.bwp),
                new Currency("KHR", "Riel camboyano", "Camboya", R.drawable.khr),
                new Currency("COP", "Peso colombiano", "Colombia", R.drawable.cop),
                new Currency("DOP", "Peso dominicano", "República Dominicana", R.drawable.dop),
                new Currency("IRR", "Rial iraní", "Irán", R.drawable.irr),
                new Currency("MYR", "Ringgit malayo", "Malasia", R.drawable.myr),
                new Currency("MUR", "Rupia de Mauricio", "Mauricio", R.drawable.mur),
                new Currency("NPR", "Rupia nepalí", "Nepal", R.drawable.npr),
                new Currency("PKR", "Rupia pakistaní", "Pakistán", R.drawable.pkr),
                new Currency("QAR", "Rial de Qatar", "Catar", R.drawable.qar),
                new Currency("SAR", "Riyal saudí", "Arabia Saudita", R.drawable.sar),
                new Currency("ZAR", "Rand sudafricano", "Sudáfrica", R.drawable.zar),
                new Currency("TOP", "Pa'anga del Reino de Tonga", "Tonga", R.drawable.top),
                new Currency("YER", "Riyal de Yemen", "Yemen", R.drawable.yer),
                new Currency("INR", "Rupia india", "India", R.drawable.inr),
                new Currency("BYR", "Rublo bielorruso", "Bielorrusia", R.drawable.byr),
                new Currency(BRL, "Real brasileño", "Brasil", R.drawable.brl),
                new Currency("CLP", "Peso chileno", "Chile", R.drawable.clp),
                new Currency("CUP", "Peso cubano", "Cuba", R.drawable.cup),
                new Currency("GTQ", "Quetzal guatemalteco", "Guatemala", R.drawable.gtq),
                new Currency("MOP", "Pataca de Macao", "Macao", R.drawable.mop),
                new Currency("MVR", "Rupia de Maldivas", "Maldivas", R.drawable.mvr),
                new Currency("MXN", "Peso mexicano", "Mexico", R.drawable.mxn),
                new Currency("OMR", "Rial omaní", "Omán", R.drawable.omr),
                new Currency("PHP", "Peso filipino", "Filipinas", R.drawable.php),
                new Currency("RUB", "Rublo ruso", "Rusia", R.drawable.rub),
                new Currency("SCR", "Rupia de Seychelles", "Seychelles", R.drawable.scr),
                new Currency("LKR", "Rupia de Sri Lanka", "Sri Lanka", R.drawable.lkr),
                new Currency(UYU, "Peso uruguayo", "Uruguay", R.drawable.uyu),
//				new Currency("ECS", "Sucre ecuatoriano", "Ecuador", R.drawable.ecs),
                new Currency("ILS", "Shéquel israelí", "Israel", R.drawable.ils),
                new Currency("BDT", "Taka bangladeshí", "Bangladesh", R.drawable.bdt),
                new Currency("MRO", "Uquiya de Mauritania", "Mauritania", R.drawable.mro),
                new Currency("WST", "Tala de Samoa", "Samoa", R.drawable.wst),
                new Currency("KZT", "Tenge kazajo", " Kazajistán", R.drawable.kzt),
                new Currency("MNT", "Tugrik mongol", "Mongolia", R.drawable.mnt),
                //new Currency("SIT", "Tólar esloveno", "Eslovenia", R.drawable.sit),
                new Currency("JPY", "Yen japonés", "Japón", R.drawable.jyp),
                new Currency("CNY", "Yuan Chino", "China", R.drawable.cny),
                new Currency("KRW", "Won surcoreano", "Corea del Sur", R.drawable.krw),
                new Currency("KPW", "Won norcoreano", "Corea del Norte", R.drawable.kpw)

        ));

        //System.out.println("Number of currencies: " + allCurrencies.size());
    }
}
