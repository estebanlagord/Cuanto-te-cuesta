package com.smartpocket.cuantoteroban;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.smartpocket.cuantoteroban.preferences.PreferencesManager;

public class CurrencyManager {
	private static CurrencyManager instance;
	Set<Currency> allCurrencies  = new TreeSet<Currency>();
	Set<Currency> userCurrencies = new TreeSet<Currency>();
	
	private CurrencyManager(){
		buildAllCurrencies();
	}
	
	public static CurrencyManager getInstance() {
		if (instance == null)
			instance = new CurrencyManager();
		
		return instance;
	}
	
	
	public Set<Currency> getAllUnusedCurrencies() {
		Set<Currency> result = new TreeSet<Currency>(allCurrencies);
		result.removeAll(getUserCurrencies());
		return result;
	}
	
	public Set<Currency> getAllCurrencies() {
		return allCurrencies;
	}
	
	public Set<Currency> getUserCurrencies() {
		List<Currency> chosenCurrencies = PreferencesManager.getInstance().getChosenCurrencies();
		userCurrencies.clear();
		userCurrencies.addAll(chosenCurrencies);
		return userCurrencies;
	}
	
	public void addToUserCurrencies(Currency curr) {
		if (getUserCurrencies().add(curr))
			PreferencesManager.getInstance().setChosenCurrencies(new ArrayList<Currency>(userCurrencies));
	}
	
	public void removeFromUserCurrencies(Currency curr) {
		if (getUserCurrencies().remove(curr))
			PreferencesManager.getInstance().setChosenCurrencies(new ArrayList<Currency>(userCurrencies));
	}
	
	public Currency findCurrency(String code) {
		Currency result = null;
		
		for (Currency curr : allCurrencies) {
			if (curr.getCode().equalsIgnoreCase(code)){
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

				new Currency("THB", "Baht tailand�s", "Tailandia", R.drawable.thb),
				new Currency("ETB", "Birr et�ope", "Etiopia", R.drawable.etb),
				new Currency("VEF", "Bol�var Fuerte Venezolano", "Venezuela", R.drawable.vef),
				new Currency("BOB", "Boliviano", "Bolivia", R.drawable.bob),
				new Currency("PAB", "Balboa paname�o", "Panam�", R.drawable.pab),
				new Currency("CRC", "Col�n costarricense", "Costa Rica", R.drawable.crc),
				new Currency("DKK", "Corona danesa", "Dinamarca", R.drawable.dkk),
				//new Currency("EEK", "Corona estonia", "Estonia", R.drawable.eek),
				new Currency("ISK", "Corona islandesa", "Islandia", R.drawable.isk),
				new Currency("NIO", "C�rdoba nicarag�ense", "Nicaragua", R.drawable.nio),
				//new Currency("SKK", "Corona eslovaca", "Eslovaquia", R.drawable.skk),
				new Currency("SEK", "Corona sueca", "Suecia", R.drawable.sek),
				new Currency("UGX", "Chel�n ugand�s", "Uganda", R.drawable.ugx),
				new Currency("CZK", "Corona checa", "Republica Checa", R.drawable.czk),
				new Currency("SVC", "Col�n de El Salvador", "El Salvador", R.drawable.svc),
				//new Currency("GHC", "Cedi ghan�s", "Ghana", R.drawable.ghc),
				new Currency("KES", "Chel�n keniano", "Kenia", R.drawable.kes),
				new Currency("NOK", "Corona noruega", "Noruega", R.drawable.nok),
				new Currency("SOS", "Chel�n somal�", "Somalia", R.drawable.sos),
				new Currency("TZS", "Chel�n tanzano", "Tanzania", R.drawable.tzs),
				new Currency("EUR", "Euro", "Union Europea", R.drawable.eur),
				new Currency("AUD", "D�lar australiano", "Australia", R.drawable.aud),
				new Currency("CHF", "Franco suizo", "Suiza", R.drawable.chf),
				new Currency("DZD", "Dinar argelino", "Argelia", R.drawable.dzd),
				new Currency("BSD", "D�lar bahame�o", "Bahamas", R.drawable.bsd),
				new Currency("BBD", "D�lar de Barbados", "Barbados", R.drawable.bbd),
				new Currency("BMD", "D�lar bermude�o", "Bermudas", R.drawable.bmd),
				new Currency("BIF", "Franco de Burundi", "Burundi", R.drawable.bif),
				//new Currency("KYD", "D�lar de las Islas Caim�n", "Islas Caim�n", R.drawable.kyd),
				new Currency("XAF", "Franco CFA (BEAC)", "�frica", "Ben�n Burkina Faso Costa de Marfil Guinea Bissau Mali N�ger Senegal Togo", R.drawable.xaf),
				new Currency("DJF", "Franco yibutiano", "Yibuti", R.drawable.djf),
				new Currency("FJD", "D�lar fiyiano", "Fiyi", R.drawable.fjd),
				new Currency("GNF", "Franco guineano", "Guinea", R.drawable.gnf),
				new Currency("HUF", "Flor�n h�ngaro", "Hungr�a", R.drawable.huf),
				new Currency("JMD", "D�lar jamaiquino", "Jamaica", R.drawable.jmd),
				new Currency("KWD", "Dinar kuwait�", "Kuwait", R.drawable.kwd),
				new Currency("LYD", "Dinar libio", "Libia", R.drawable.lyd),
				new Currency("MAD", "Dirham marroqu�", "Marruecos", R.drawable.mad),
				new Currency("ANG", "Flor�n antillano neerland�s", "Curazao / Sint Maarten", "Curasao Cura�ao", R.drawable.ang),
				new Currency("XPF", "Franco del Pac�fico", " Polinesia Francesa / Nueva Caledonia / Wallis y Futuna", R.drawable.xpf),
				new Currency("STD", "Dobra de Santo Tom�", "Santo Tom�", R.drawable.std),
				new Currency("SBD", "D�lar de las Islas Salom�n", "Islas Salom�n", R.drawable.sbd),
				new Currency("TTD", "D�lar trinitense", "Trinidad y Tobago", R.drawable.ttd),
				new Currency("AED", "Dirham de los Emiratos �rabes Unidos", "Emiratos �rabes Unidos", R.drawable.aed),
				//new Currency("ZWD", "D�lar zimbabuense", "Zimbabue", R.drawable.zwd),
				new Currency("USD", "D�lar estadounidense", "Estados Unidos", "EEUU EE.UU. USA U.S.A", R.drawable.usd),
				new Currency("CAD", "D�lar canadiense", "Canada", R.drawable.cad),
				new Currency("HKD", "D�lar de Hong Kong", "Hong Kong", R.drawable.hkd),
				new Currency("AWG", "Flor�n arube�o", "Aruba", R.drawable.awg),
				new Currency("BHD", "Dinar bahrein�", "Bar�in", R.drawable.bhd),
				new Currency("BZD", "D�lar belice�o", "Belice", "Belize", R.drawable.bzd),
				new Currency("BND", "D�lar de Brun�i", "Brun�i", R.drawable.bnd),
				new Currency("CVE", "Escudo caboverdiano", "Cabo Verde", R.drawable.cve),
				new Currency("XOF", "Franco CFA (BEAO)", "�frica", "Camer�n Chad Gab�n Guinea Ecuatorial Rep�blica Centroafricana Rep�blica del Congo", R.drawable.xof),
				new Currency("KMF", "Franco comorano", "Comoras", R.drawable.kmf),
				new Currency("XCD", "D�lar del Caribe Oriental", "Antigua y Barbuda / Dominica / Granada / San Crist�bal y Nieves / Santa Luc�a / San Vicente y las Granadinas / Anguila / Montserrat", "Caribe", R.drawable.xcd),
				new Currency("GMD", "Dalasi gambiano", "Gambia", R.drawable.gmd),
				new Currency("GYD", "D�lar guyan�s", "Guyana", R.drawable.gyd),
				new Currency("IQD", "Dinar iraqu�", "Irak", R.drawable.iqd),
				new Currency("JOD", "Dinar jordano", "Jordania", R.drawable.jod),
				new Currency("LRD", "D�lar liberiano", "Liberia", R.drawable.lrd),
				new Currency("MKD", "Dinar macedonio", "Macedonia", R.drawable.mkd),
				new Currency("NAD", "D�lar namibio", "Namibia", R.drawable.nad),
				new Currency("NZD", "D�lar neozeland�s", "Nueva Zelanda", R.drawable.nzd),
				new Currency("RWF", "Franco ruand�s", "Ruanda", R.drawable.rwf),
				new Currency("SGD", "D�lar de Singapur", "Singapur", R.drawable.sgd),
				new Currency("TWD", "D�lar taiwan�s", "Taiwan", R.drawable.twd),
				new Currency("TND", "Dinar tunecino", "T�nez", R.drawable.tnd),
				new Currency("VND", "Dong de Vietnam", "Vietnam", R.drawable.vnd),
				new Currency("HTG", "Gourde haitiano", "Hait�", R.drawable.htg),
				new Currency("UAH", "Grivnia ucraniana", "Ucrania", R.drawable.uah),
				new Currency("PYG", "Guaran� paraguayo", "Paraguay", R.drawable.pyg),
				new Currency("GBP", "Libra esterlina", "Gran Breta�a", "Inglaterra", R.drawable.gbp),
				new Currency("BGN", "Lev b�lgaro", "Bulgaria", R.drawable.bgn),
				new Currency("HRK", "Kuna croata", "Croacia", R.drawable.hrk),
				new Currency("FKP", "Libra malvinense", "Islas Malvinas", " Islas Georgias del Sur Sandwich del Sur", R.drawable.fkp),
				new Currency("HNL", "Lempira hondure�a", "Honduras", R.drawable.hnl),
				new Currency("LVL", "Lats let�n", "Letonia", R.drawable.lvl),
				new Currency("LSL", "Loti lesotense", "Lesoto", R.drawable.lsl),
				new Currency("MWK", "Kwacha malau�", "Malaui", R.drawable.mwk),
				new Currency("MDL", "Leu moldavo", "Moldavia", R.drawable.mdl),
				new Currency("TRY", "Lira turca", " Turqu�a / Rep�blica Turca del Norte de Chipre", R.drawable.try_turquia),
				new Currency("RON", "Leu rumano", "Rumania", R.drawable.ron),
				new Currency("SHP", "Libra de Santa Helena", " Santa Elena /  Ascensi�n / Trist�n de Acu�a", R.drawable.shp),
				new Currency("SZL", "Lilangeni de Suazilandia", "Suazilandia", R.drawable.szl),
				new Currency("ZMK", "Kwacha zambiano", "Zambia", R.drawable.zmk),
				new Currency("ALL", "Lek alban�s", "Albania", R.drawable.all),
				new Currency("EGP", "Libra egipcia", "Egipto", R.drawable.egp),
				new Currency("GIP", "Libra gibraltare�a", "Gibraltar", R.drawable.gip),
				new Currency("LAK", "Kip laosiano", "Laos", R.drawable.lak),
				new Currency("LBP", "Libra libanesa", "L�bano", R.drawable.lbp),
				new Currency("LTL", "Litas lituana", "Lituania", R.drawable.ltl),
				//new Currency("MTL", "Lira maltesa", "Malta", R.drawable.mtl),
				new Currency("MMK", "Kyat birmano", "Birmania", "Uni�n de Myanmar", R.drawable.mmk),
				new Currency("PGK", "Kina de Pap�a Nueva Guinea", "Pap�a Nueva Guinea", R.drawable.pgk),
				new Currency("SLL", "Leone de Sierra Leona", "Sierra Leona", R.drawable.sll),
				new Currency("SDG", "Dinar sudan�s", "Sud�n", R.drawable.sdg),
				new Currency("SYP", "Libra siria", "Siria", R.drawable.syp),
				//new Currency("ERN", "Nakfa de Eritrea", "Eritrea", R.drawable.ern),
				new Currency("NGN", "Naira nigeriano", "Nigeria", R.drawable.ngn),
				new Currency("PEN", "Nuevo sol peruano", "Per�", R.drawable.pen),
				new Currency("BTN", "Ngultrum butan�s", "But�n", R.drawable.btn),
				new Currency("IDR", "Rupia indonesia", "Indonesia", R.drawable.idr),
				new Currency("BWP", "Pula botsuana", "Botsuana", R.drawable.bwp),
				new Currency("KHR", "Riel camboyano", "Camboya", R.drawable.khr),
				new Currency("COP", "Peso colombiano", "Colombia", R.drawable.cop),
				new Currency("DOP", "Peso dominicano", "Rep�blica Dominicana", R.drawable.dop),
				new Currency("IRR", "Rial iran�", "Ir�n", R.drawable.irr),
				new Currency("MYR", "Ringgit malayo", "Malasia", R.drawable.myr),
				new Currency("MUR", "Rupia de Mauricio", "Mauricio", R.drawable.mur),
				new Currency("NPR", "Rupia nepal�", "Nepal", R.drawable.npr),
				new Currency("PKR", "Rupia pakistan�", "Pakist�n", R.drawable.pkr),
				new Currency("QAR", "Rial de Qatar", "Catar", R.drawable.qar),
				new Currency("SAR", "Riyal saud�", "Arabia Saudita", R.drawable.sar),
				new Currency("ZAR", "Rand sudafricano", "Sud�frica", R.drawable.zar),
				new Currency("TOP", "Pa'anga del Reino de Tonga", "Tonga", R.drawable.top),
				new Currency("YER", "Riyal de Yemen", "Yemen", R.drawable.yer),
				new Currency("INR", "Rupia india", "India", R.drawable.inr),
				new Currency("BYR", "Rublo bielorruso", "Bielorrusia", R.drawable.byr),
				new Currency("BRL", "Real brasile�o", "Brasil", R.drawable.brl),
				new Currency("CLP", "Peso chileno", "Chile", R.drawable.clp),
				new Currency("CUP", "Peso cubano", "Cuba", R.drawable.cup),
				new Currency("GTQ", "Quetzal guatemalteco", "Guatemala", R.drawable.gtq),
				new Currency("MOP", "Pataca de Macao", "Macao", R.drawable.mop),
				new Currency("MVR", "Rupia de Maldivas", "Maldivas", R.drawable.mvr),
				new Currency("MXN", "Peso mexicano", "Mexico", R.drawable.mxn),
				new Currency("OMR", "Rial oman�", "Om�n", R.drawable.omr),
				new Currency("PHP", "Peso filipino", "Filipinas", R.drawable.php),
				new Currency("RUB", "Rublo ruso", "Rusia", R.drawable.rub),
				new Currency("SCR", "Rupia de Seychelles", "Seychelles", R.drawable.scr),
				new Currency("LKR", "Rupia de Sri Lanka", "Sri Lanka", R.drawable.lkr),
				new Currency("UYU", "Peso uruguayo", "Uruguay", R.drawable.uyu),
				new Currency("ECS", "Sucre ecuatoriano", "Ecuador", R.drawable.ecs),
				new Currency("ILS", "Sh�quel israel�", "Israel", R.drawable.ils),
				new Currency("BDT", "Taka bangladesh�", "Bangladesh", R.drawable.bdt),
				new Currency("MRO", "Uquiya de Mauritania", "Mauritania", R.drawable.mro),
				new Currency("WST", "Tala de Samoa", "Samoa", R.drawable.wst),
				new Currency("KZT", "Tenge kazajo", " Kazajist�n", R.drawable.kzt),
				new Currency("MNT", "Tugrik mongol", "Mongolia", R.drawable.mnt),
				//new Currency("SIT", "T�lar esloveno", "Eslovenia", R.drawable.sit),
				new Currency("JPY", "Yen japon�s", "Jap�n", R.drawable.jyp),
				new Currency("CNY", "Yuan Chino", "China", R.drawable.cny),
				new Currency("KRW", "Won surcoreano", "Corea del Sur", R.drawable.krw),
				new Currency("KPW", "Won norcoreano", "Corea del Norte", R.drawable.kpw)
				
				));
		
		//System.out.println("Number of currencies: " + allCurrencies.size());
	}
}
