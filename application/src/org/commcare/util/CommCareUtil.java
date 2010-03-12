/**
 * 
 */
package org.commcare.util;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.commcare.applogic.CommCareCaseSelectState;
import org.commcare.applogic.CommCareFormEntryState;
import org.commcare.applogic.CommCareHomeState;
import org.commcare.applogic.CommCareSelectState;
import org.commcare.core.properties.CommCareProperties;
import org.commcare.entity.CaseInstanceLoader;
import org.commcare.entity.CommCareEntity;
import org.commcare.entity.ReferralInstanceLoader;
import org.commcare.suite.model.Entry;
import org.commcare.suite.model.Suite;
import org.javarosa.cases.model.Case;
import org.javarosa.chsreferral.model.PatientReferral;
import org.javarosa.chsreferral.util.IPatientReferralFilter;
import org.javarosa.core.api.State;
import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.services.Logger;
import org.javarosa.core.services.PropertyManager;
import org.javarosa.core.services.storage.IStorageUtility;
import org.javarosa.core.services.storage.IStorageUtilityIndexed;
import org.javarosa.core.services.storage.StorageManager;
import org.javarosa.core.util.PropertyUtils;
import org.javarosa.entity.model.Entity;
import org.javarosa.j2me.view.J2MEDisplay;
import org.javarosa.services.transport.TransportService;

/**
 * @author ctsims
 *
 */
public class CommCareUtil {
	
	
	private static final String COMMCARE_RELEASE_PROPERTY = "CommCare-Release";
	private final static String PROP_APP_VERSION = "App-Version";
	private final static String PROP_JR_BUILD_VERSION = "JR-Build-Version";
	private final static String PROP_CC_CORE_BUILD_VERSION = "CC-Core-Build-Version";
	private final static String PROP_CC_DEPLOY_BUILD_VERSION = "CC-Deploy-Build-Version";
	private final static String PROP_POLISH_VERSION = "Polish-Version";
	private final static String PROP_POLISH_DEVICE = "Polish-Device";
	private final static String PROP_BUILD_DATE = "Built-on";
	private final static String PROP_RELEASE_DATE = "Released-on";
	private final static String PROP_BUILD_NUM = "Build-Number";
	private final static String PROP_PROFILE_REFERENCE = "Profile";
	
	public final static int VERSION_SHORT = 1;
	public final static int VERSION_MED = 2;
	public final static int VERSION_LONG = 3;
	
	/**
	 * 
	 * @param defaultPostURL
	 */
	public static void initializePostURL(String defaultPostURL) {
		Vector urls = PropertyManager._().getProperty(CommCareProperties.POST_URL_LIST_PROPERTY);
		
		//Default behavior for clean phones.
		if(urls == null || urls.size() == 0) {
			PropertyUtils.initializeProperty(CommCareProperties.POST_URL_LIST_PROPERTY, defaultPostURL);
			PropertyUtils.initializeProperty(CommCareProperties.POST_URL_PROPERTY, defaultPostURL);
		} else {
			//There is at least one URL on the phone.
			if(urls.contains(defaultPostURL)) {
				//The default URL is on the phone. 
			} else {
				//The default URL isn't on the phone, which means we're upgrading. Add it to the list and make it the
				//default
				urls.addElement(defaultPostURL);
				PropertyManager._().setProperty(CommCareProperties.POST_URL_LIST_PROPERTY, urls);
				
				PropertyManager._().setProperty(CommCareProperties.POST_URL_PROPERTY, defaultPostURL);
			}			
		}
	}
	
	
	public static int getNumberUnsent() {
		return TransportService.getCachedMessagesSize();
	}
	
	public static String getProfileReference() {
		return getAppProperty(PROP_PROFILE_REFERENCE);
	}
		
	public static String getAppProperty (String key) {
		return CommCareContext._().getMidlet().getAppProperty(key);
	}

	public static String getAppProperty (String key, String defaultValue) {
		String prop = getAppProperty(key);
		return (prop != null ? prop : defaultValue);
	}
	
	public static String getVersion () {
		return getVersion(VERSION_LONG);
	}
	
	public static String getVersion (int type) {
		final int hashLength = 6;
		String vApp = getAppProperty(PROP_APP_VERSION, "??");
		String vBuildJR = getAppProperty(PROP_JR_BUILD_VERSION, "??");
		String vBuildCCCore = getAppProperty(PROP_CC_CORE_BUILD_VERSION, "??");
		String vBuildCCDeploy = getAppProperty(PROP_CC_DEPLOY_BUILD_VERSION, "??");
		String vPolish = getAppProperty(PROP_POLISH_VERSION, "??");
		String vDevice = getAppProperty(PROP_POLISH_DEVICE, "??");
		String buildDate = getAppProperty(PROP_BUILD_DATE, "??");
		String releaseDate = getAppProperty(PROP_RELEASE_DATE, "--");
		String buildNum = getAppProperty(PROP_BUILD_NUM);
		boolean released = !releaseDate.equals("--");
		
		vBuildJR = vBuildJR.substring(0, Math.min(hashLength, vBuildJR.length()));
		vBuildCCCore = vBuildCCCore.substring(0, Math.min(hashLength, vBuildCCCore.length()));
		vBuildCCDeploy = vBuildCCDeploy.substring(0, Math.min(hashLength, vBuildCCDeploy.length()));
		vPolish = (String)DateUtils.split(vPolish, " ", true).elementAt(0);
		buildDate = (String)DateUtils.split(buildDate, " ", true).elementAt(0);
		releaseDate = (String)DateUtils.split(releaseDate, " ", true).elementAt(0);
		
		switch (type) {
		case VERSION_LONG:
			return vApp + " (" + vBuildJR + "-" + vBuildCCCore + "-" + vBuildCCDeploy + "-" + vPolish + "-" + vDevice +
				")" + (released ? " #" + buildNum : "") + " b:" + buildDate + " r:" + releaseDate;
		case VERSION_MED:
			return vApp + " " + (released ? "#" + buildNum + " (" + releaseDate + ")" : "<unreleased>");
		case VERSION_SHORT:
			return vApp;
		default: throw new RuntimeException("unknown version type");
		}
	}
		
	public static IPatientReferralFilter overdueFilter(final String caseType) {
		return new IPatientReferralFilter() {
			public boolean inFilter(PatientReferral ref) {
				Date due = ref.getDateDue();		
				String caseTypeId = getCase(ref.getLinkedId()).getTypeId();
				return (due != null && DateUtils.dateDiff(due, DateUtils.today()) >= 0 &&
						(caseType == null || caseTypeId.equals(caseType)));
			}
		};
	}
	
	public static Case getCase (int recordId) {
		IStorageUtility cases = StorageManager.getStorage(Case.STORAGE_KEY);
		return (Case)cases.read(recordId);
	}
	
	public static Case getCase (String caseId) {
		IStorageUtilityIndexed cases = (IStorageUtilityIndexed)StorageManager.getStorage(Case.STORAGE_KEY);
		return (Case)cases.getRecordForValue("case-id", caseId);
	}
	 
	public static PatientReferral getReferral (int id) {
		IStorageUtility referrals = StorageManager.getStorage(PatientReferral.STORAGE_KEY);
		return (PatientReferral)referrals.read(id);
	}
		
	public static boolean isTestingMode() {
		return !"true".equals(CommCareUtil.getAppProperty(COMMCARE_RELEASE_PROPERTY));
	}
			
	public static void exit () {
		Logger.log("app-close", "");
		CommCareContext._().getMidlet().notifyDestroyed();
	}
	
	public static void launchHomeState() {
		J2MEDisplay.startStateWithLoadingScreen(new CommCareHomeState());
	}
	
	public static void launchEntry(Suite suite, Entry entry, State returnState) {
		final Entry e = entry;
		final State s = returnState;
		
		Hashtable<String, String> references = entry.getReferences();
		if(references.size() == 0) {
			String namespace = entry.getXFormNamespace();
			CommCareFormEntryState state = new CommCareFormEntryState(namespace, CommCareContext._().getPreloaders(), CommCareContext._().getFuncHandlers()) {
				protected void goHome() {
					J2MEDisplay.startStateWithLoadingScreen(s);			
				}
			};
			J2MEDisplay.startStateWithLoadingScreen(state);
		}
		else {
			//this will be revisited and rewritten 
			boolean referral = false;
			// Need to do some reference gathering... 
			for(Enumeration en = references.keys() ; en.hasMoreElements() ; ) {
				String key = (String)en.nextElement();
				String refType = references.get(key);
				if(refType.toLowerCase().equals("referral")) {
					referral = true;
				}
			}
			State select = null;
			if(referral) {
				Entity<PatientReferral> entity = new CommCareEntity<PatientReferral>(entry,suite.getDetail(entry.getShortDetailId()), suite.getDetail(entry.getLongDetailId()), new ReferralInstanceLoader(e.getReferences()));
				select = new CommCareSelectState<PatientReferral>(entity, PatientReferral.STORAGE_KEY) {
					
					public void cancel() {
						J2MEDisplay.startStateWithLoadingScreen(s);
					}
					
					public void entitySelected(int id) {
						CommCareFormEntryState state = new CommCareFormEntryState(e.getXFormNamespace(), CommCareContext._().getPreloaders(CommCareUtil.getReferral(id)), CommCareContext._().getFuncHandlers()) {
							protected void goHome() {
								J2MEDisplay.startStateWithLoadingScreen(s);
							}
						};
						J2MEDisplay.startStateWithLoadingScreen(state);
					}
				};
			} else {
				Entity<Case> entity = new CommCareEntity<Case>(entry,suite.getDetail(entry.getShortDetailId()), suite.getDetail(entry.getLongDetailId()), new CaseInstanceLoader(e.getReferences()));
				select = new CommCareSelectState<Case>(entity,Case.STORAGE_KEY) {
					
					public void cancel() {
						J2MEDisplay.startStateWithLoadingScreen(s);
					}
					
					public void entitySelected(int id) {
						String form = e.getXFormNamespace();
						State state;
						if(form == null) {
							state = s;
						} else {
							state = new CommCareFormEntryState(form, CommCareContext._().getPreloaders(CommCareUtil.getCase(id)), CommCareContext._().getFuncHandlers()) {
								protected void goHome() {
									J2MEDisplay.startStateWithLoadingScreen(s);					
								}
							};
						}
						J2MEDisplay.startStateWithLoadingScreen(state);
					}
				};
			}
			J2MEDisplay.startStateWithLoadingScreen(select);
		}
	}
}
