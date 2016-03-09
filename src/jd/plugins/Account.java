//    jDownloader - Downloadmanager
//    Copyright (C) 2008  JD-Team support@jdownloader.org
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jd.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.appwork.storage.JSonStorage;
import org.appwork.storage.TypeRef;
import org.appwork.storage.config.annotations.LabelInterface;
import org.appwork.utils.Hash;
import org.appwork.utils.StringUtils;
import org.jdownloader.controlling.UniqueAlltimeID;
import org.jdownloader.logging.LogController;
import org.jdownloader.settings.staticreferences.CFG_GENERAL;
import org.jdownloader.translate._JDT;

import jd.config.Property;
import jd.controlling.AccountController;
import jd.http.Cookie;
import jd.http.Cookies;

public class Account extends Property {

    private static final String VALID_UNTIL                    = "VALID_UNTIL";

    private static final String ACCOUNT_TYPE                   = "ACCOUNT_TYPE";

    private static final String LATEST_VALID_TIMESTAMP         = "LATEST_VALID_TIMESTAMP";

    public static final String  IS_MULTI_HOSTER_ACCOUNT        = "IS_MULTI_HOSTER_ACCOUNT";

    private static final long   serialVersionUID               = -7578649066389032068L;

    private String              user;
    private String              pass;

    private boolean             enabled                        = true;
    private boolean             concurrentUsePossible          = true;

    public static final String  PROPERTY_TEMP_DISABLED_TIMEOUT = "PROPERTY_TEMP_DISABLED_TIMEOUT";
    public static final String  PROPERTY_REFRESH_TIMEOUT       = "PROPERTY_REFRESH_TIMEOUT";

    private static final String COOKIE_STORAGE                 = "COOKIE_STORAGE";

    public boolean isConcurrentUsePossible() {
        return concurrentUsePossible;
    }

    public synchronized void saveCookies(final Cookies cookies, final String ID) {
        final String validation = Hash.getSHA256(getUser() + ":" + getPass());
        final List<CookieStorable> cookieStorables = new ArrayList<CookieStorable>();
        // do not cache antiddos cookies, this is job of the antiddos module, otherwise will can and will cause conflicts!
        final String antiddosCookies = jd.plugins.hoster.antiDDoSForHost.antiDDoSCookiePattern;
        for (final Cookie cookie : cookies.getCookies()) {
            if (cookie.getKey() != null && !cookie.getKey().matches(antiddosCookies)) {
                cookieStorables.add(new CookieStorable(cookie));
            }
        }
        setProperty(COOKIE_STORAGE, validation);
        final String COOKIE_STORAGE_ID = COOKIE_STORAGE + ":" + ID;
        setProperty(COOKIE_STORAGE_ID, JSonStorage.toString(cookieStorables));
        final String COOKIE_STORAGE_TIMESTAMP_ID = COOKIE_STORAGE + ":TS:" + ID;
        setProperty(COOKIE_STORAGE_TIMESTAMP_ID, System.currentTimeMillis());
    }

    public synchronized void clearCookies(final String ID) {
        removeProperty(COOKIE_STORAGE);
        final String COOKIE_STORAGE_ID = COOKIE_STORAGE + ":" + ID;
        removeProperty(COOKIE_STORAGE_ID);
        final String COOKIE_STORAGE_TIMESTAMP_ID = COOKIE_STORAGE + ":TS:" + ID;
        removeProperty(COOKIE_STORAGE_TIMESTAMP_ID);
    }

    public synchronized long getCookiesTimeStamp(final String ID) {
        final String COOKIE_STORAGE_TIMESTAMP_ID = COOKIE_STORAGE + ":TS:" + ID;
        return getLongProperty(COOKIE_STORAGE_TIMESTAMP_ID, -1);
    }

    public synchronized Cookies loadCookies(final String ID) {
        final String validation = Hash.getSHA256(getUser() + ":" + getPass());
        if (StringUtils.equals(getStringProperty(COOKIE_STORAGE), validation)) {
            final String COOKIE_STORAGE_ID = COOKIE_STORAGE + ":" + ID;
            final String cookieStorables = getStringProperty(COOKIE_STORAGE_ID);
            if (StringUtils.isNotEmpty(cookieStorables)) {
                try {
                    final List<CookieStorable> cookies = JSonStorage.restoreFromString(cookieStorables, new TypeRef<ArrayList<CookieStorable>>() {
                    }, null);
                    final Cookies ret = new Cookies();
                    for (final CookieStorable storable : cookies) {
                        ret.add(storable._restore());
                    }
                    return ret;
                } catch (Throwable e) {
                    LogController.CL().log(e);
                }
            }
        }
        clearCookies(ID);
        return null;
    }

    /**
     * @since JD2
     */
    public void setConcurrentUsePossible(boolean concurrentUsePossible) {
        this.concurrentUsePossible = concurrentUsePossible;
    }

    private transient volatile long tmpDisabledTimeout = -1;

    public long getTmpDisabledTimeout() {
        return Math.max(-1, tmpDisabledTimeout);
    }

    private transient UniqueAlltimeID id            = new UniqueAlltimeID();

    /* keep for comp. reasons */
    private String                    hoster        = null;
    private List<String>              hosterHistory = null;

    public List<String> getHosterHistory() {
        return hosterHistory;
    }

    public void setHosterHistory(List<String> hosterHistory) {
        if (hosterHistory != null && hosterHistory.size() > 0) {
            this.hosterHistory = new CopyOnWriteArrayList<String>(hosterHistory);
        } else {
            this.hosterHistory = null;
        }
    }

    private AccountInfo                     accinfo      = null;

    private long                            updatetime   = 0;
    private int                             maxDownloads = 0;

    private transient AccountController     ac           = null;
    private transient PluginForHost         plugin       = null;
    private transient boolean               isMulti      = false;

    private transient volatile AccountError error;

    private transient volatile String       errorString;

    public PluginForHost getPlugin() {
        return plugin;
    }

    public void setPlugin(PluginForHost plugin) {
        this.plugin = plugin;
    }

    /**
     *
     * @param string
     * @return
     */
    private static final String trim(final String string) {
        return (string == null) ? null : string.trim();
    }

    public void setAccountController(AccountController ac) {
        this.ac = ac;
    }

    public AccountController getAccountController() {
        return ac;
    }

    /**
     *
     * @param user
     * @param pass
     */
    public Account(final String user, final String pass) {
        this.user = trim(user);
        this.pass = trim(pass);
    }

    public int getMaxSimultanDownloads() {
        return maxDownloads;
    }

    /**
     * -1 = unlimited, 0 = use deprecated getMaxSimultanPremiumDownloadNum/getMaxSimultanFreeDownloadNum,>1 = use this
     *
     * @since JD2
     */
    public void setMaxSimultanDownloads(int max) {
        if (max < 0) {
            maxDownloads = -1;
        } else {
            maxDownloads = max;
        }
    }

    public String getPass() {
        return pass;
    }

    public boolean isValid() {
        final AccountError lerror = getError();
        return lerror == null || AccountError.TEMP_DISABLED.equals(lerror);
    }

    public long getLastValidTimestamp() {
        return getLongProperty(LATEST_VALID_TIMESTAMP, -1);
    }

    /**
     * @Deprecated Use #setError
     * @param b
     */
    @Deprecated
    public void setValid(final boolean b) {
        if (b) {
            if (getError() == AccountError.INVALID) {
                setError(null, null);
            }
        } else {
            setError(AccountError.INVALID, null);
        }
    }

    public long lastUpdateTime() {
        return updatetime;
    }

    public void setUpdateTime(final long l) {
        updatetime = l;
    }

    public String getHoster() {
        return hoster;
    }

    public void setHoster(final String h) {
        hoster = h;
    }

    private AtomicBoolean checking = new AtomicBoolean(false);

    public void setChecking(boolean b) {
        checking.set(b);
    }

    public boolean isChecking() {
        return checking.get();
    }

    public AccountInfo getAccountInfo() {
        return accinfo;
    }

    public void setAccountInfo(final AccountInfo info) {
        accinfo = info;
        if (info != null) {
            if (AccountType.PREMIUM.equals(getType()) && !info.isExpired() && info.getValidUntil() > 0) {
                setValidPremiumUntil(info.getValidUntil());
            }
        }
    }

    /**
     * The expire Date of a premiumaccount. if the account is not a premiumaccount any more, this timestamp points to the last valid expire
     * date. it can be used to check when an account has expired
     *
     * @param validUntil
     */
    private void setValidPremiumUntil(long validUntil) {
        setProperty(VALID_UNTIL, validUntil);
    }

    /**
     * this method returns for how long this account will be (or has been) a premium account
     *
     * The expire Date of a premiumaccount. if the account is not a premiumaccount any more, this timestamp points to the last valid expire
     * date. it can be used to check when an account has expired
     *
     * @param validUntil
     *
     * @return
     */
    public long getValidPremiumUntil() {
        AccountInfo info = getAccountInfo();
        long ret = -1;
        if (info != null) {
            if (AccountType.PREMIUM.equals(getType()) && !info.isExpired()) {
                ret = info.getValidUntil();
            }
        }

        if (ret <= 0) {
            ret = getLongProperty(VALID_UNTIL, 0);
        }
        return ret;
    }

    public String getUser() {
        return user;
    }

    public boolean isEnabled() {
        return enabled;
    }

    private void readObject(final java.io.ObjectInputStream stream) throws java.io.IOException, ClassNotFoundException {
        /* nach dem deserialisieren sollen die transienten neu geholt werden */
        stream.defaultReadObject();
        tmpDisabledTimeout = -1;
        isMulti = false;
        id = new UniqueAlltimeID();
    }

    public UniqueAlltimeID getId() {
        return id;
    }

    public void setId(long id) {
        if (id > 0) {
            this.id = new UniqueAlltimeID(id);
        }
    }

    public boolean isTempDisabled() {
        if (AccountError.TEMP_DISABLED.equals(getError())) {
            synchronized (this) {
                if (getTmpDisabledTimeout() < 0 || System.currentTimeMillis() >= getTmpDisabledTimeout()) {
                    tmpDisabledTimeout = -1;
                    setTempDisabled(false);
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    public static enum AccountError {
        TEMP_DISABLED,
        EXPIRED,
        INVALID,
        PLUGIN_ERROR;
    }

    public void setError(final AccountError error, String errorString) {
        if (error == null) {
            errorString = null;
        }
        if (this.error != error || !StringUtils.equals(this.errorString, errorString)) {
            if (AccountError.TEMP_DISABLED.equals(error)) {
                long defaultTmpDisabledTimeOut = CFG_GENERAL.CFG.getAccountTemporarilyDisabledDefaultTimeout();
                Long timeout = this.getLongProperty(PROPERTY_TEMP_DISABLED_TIMEOUT, defaultTmpDisabledTimeOut);
                if (timeout == null || timeout <= 0) {
                    timeout = defaultTmpDisabledTimeOut;
                }
                this.tmpDisabledTimeout = System.currentTimeMillis() + timeout;
            } else {
                this.tmpDisabledTimeout = -1;
            }
            this.error = error;
            this.errorString = errorString;
            notifyUpdate(AccountProperty.Property.ERROR, error);
        }
    }

    public void setTmpDisabledTimeout(long tmpDisabledTimeout) {
        this.tmpDisabledTimeout = tmpDisabledTimeout;
    }

    public AccountError getError() {
        return error;
    }

    public String getErrorString() {
        return errorString;
    }

    public void setEnabled(final boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            final AccountInfo ai = accinfo;
            if (enabled && (!isValid() || ai != null && ai.isExpired())) {
                setUpdateTime(0);
            }
            notifyUpdate(AccountProperty.Property.ENABLED, enabled);
        }
    }

    public long getRefreshTimeout() {
        /* default refresh timeout is 30 mins */
        long defaultRefreshTimeOut = 30 * 60 * 1000l;
        Long timeout = this.getLongProperty(PROPERTY_REFRESH_TIMEOUT, defaultRefreshTimeOut);
        if (timeout == null || timeout <= 0) {
            timeout = defaultRefreshTimeOut;
        }
        return timeout;
    }

    public boolean refreshTimeoutReached() {
        if (updatetime <= 0) {
            return true;
        }
        return System.currentTimeMillis() - updatetime >= getRefreshTimeout();
    }

    public static interface AccountPropertyChangeHandler {
        boolean fireAccountPropertyChange(AccountProperty property);
    }

    private transient volatile AccountPropertyChangeHandler notifyHandler = null;

    public void setNotifyHandler(AccountPropertyChangeHandler notifyHandler) {
        this.notifyHandler = notifyHandler;
    }

    private void notifyUpdate(AccountProperty.Property property, Object value) {
        AccountPropertyChangeHandler notify = notifyHandler;
        boolean notifyController = true;
        AccountProperty event = null;
        if (notify != null) {
            event = new AccountProperty(this, property, value);
            notifyController = notify.fireAccountPropertyChange(event);
        }
        notify = getAccountController();
        if (notify != null && notifyController) {
            if (event == null) {
                event = new AccountProperty(this, property, value);
            }
            notifyController = notify.fireAccountPropertyChange(event);
        }
    }

    public void setPass(String newPass) {
        newPass = trim(newPass);
        if (!StringUtils.equals(this.pass, newPass)) {
            this.pass = newPass;
            accinfo = null;
            setUpdateTime(0);
            notifyUpdate(AccountProperty.Property.PASSWORD, newPass);
        }
    }

    public void setTempDisabled(final boolean tempDisabled) {
        if (tempDisabled) {
            setError(AccountError.TEMP_DISABLED, null);
        } else {
            if (AccountError.TEMP_DISABLED.equals(getError())) {
                setError(null, null);
            }
        }
    }

    public void setUser(String newUser) {
        newUser = trim(newUser);
        if (!StringUtils.equals(this.user, newUser)) {
            this.user = newUser;
            accinfo = null;
            setUpdateTime(0);
            notifyUpdate(AccountProperty.Property.USERNAME, newUser);
        }
    }

    public String toString() {
        AccountInfo ai = this.accinfo;
        if (ai != null) {
            return user + ":" + pass + "@" + hoster + "=" + enabled + " " + super.toString() + " AccInfo: " + ai.toString();
        } else {
            return user + ":" + pass + "@" + hoster + "=" + enabled + " " + super.toString();
        }
    }

    public boolean equals(final Account account) {
        if (account == null) {
            return false;
        }
        if (account == this) {
            return true;
        }
        if (!StringUtils.equals(getHoster(), account.getHoster())) {
            return false;
        }
        if (!StringUtils.equals(getUser(), account.getUser())) {
            return false;
        }
        if (!StringUtils.equals(getPass(), account.getPass())) {
            return false;
        }
        return true;
    }

    public boolean isMulti() {
        return isMulti;
    }

    public void setLastValidTimestamp(long currentTimeMillis) {
        setProperty(LATEST_VALID_TIMESTAMP, currentTimeMillis);
    }

    public static enum AccountType
            implements LabelInterface {
        FREE {
            @Override
            public String getLabel() {
                return _JDT.T.AccountType_free();
            }
        },
        PREMIUM {
            @Override
            public String getLabel() {
                return _JDT.T.AccountType_premium();
            }
        },
        LIFETIME {
            @Override
            public String getLabel() {
                return _JDT.T.AccountType_lifetime();
            }
        },
        UNKNOWN {
            @Override
            public String getLabel() {
                return _JDT.T.AccountType_unknown();
            }
        }
    }

    /**
     * JD2 Code!
     *
     * @since JD2
     */
    public void setType(AccountType type) {
        if (type == null) {
            super.setProperty(ACCOUNT_TYPE, Property.NULL);
        } else {
            super.setProperty(ACCOUNT_TYPE, type.name());
        }
    }

    @Override
    public boolean setProperty(String key, Object value) {
        if (IS_MULTI_HOSTER_ACCOUNT.equalsIgnoreCase(key)) {
            isMulti = value != null && Boolean.TRUE.equals(value);
        } else {
            if (Property.NULL != value) {
                if ("nopremium".equalsIgnoreCase(key)) {
                    // convert.. some day we will use the setType only. The earlier we start to the correct fields, the better
                    if (Boolean.TRUE.equals(value)) {
                        setType(AccountType.FREE);
                    } else {
                        setType(AccountType.PREMIUM);
                    }
                } else if ("free".equalsIgnoreCase(key)) {
                    if (Boolean.TRUE.equals(value)) {
                        setType(AccountType.FREE);
                    } else {
                        setType(AccountType.PREMIUM);
                    }
                } else if ("session_type".equalsIgnoreCase(key)) {
                    if (!"premium".equals(value)) {
                        setType(AccountType.FREE);
                    } else {
                        setType(AccountType.PREMIUM);
                    }
                } else if ("premium".equalsIgnoreCase(key)) {
                    if (Boolean.TRUE.equals(value)) {
                        setType(AccountType.PREMIUM);
                    } else {
                        setType(AccountType.FREE);
                    }
                }
            }
        }
        return super.setProperty(key, value);
    }

    private final static String REGTS = "regts";

    public long getRegisterTimeStamp() {
        return getLongProperty(REGTS, -1);
    }

    public void setRegisterTimeStamp(long ts) {
        if (ts < 0) {
            removeProperty(REGTS);
        } else {
            setProperty(REGTS, ts);
        }
    }

    public AccountType getType() {
        final String v = getStringProperty(ACCOUNT_TYPE, null);
        if (v != null) {
            try {
                return AccountType.valueOf(v);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        if (getBooleanProperty("nopremium", false)) {
            // nopremium z.b. 4shared.com
            return AccountType.FREE;
        } else if (getBooleanProperty("free", false)) {
            return AccountType.FREE;
        } else if (getBooleanProperty("premium", false)) {
            return AccountType.PREMIUM;
        } else if (getBooleanProperty("PREMIUM", false)) {
            return AccountType.PREMIUM;
        } else if (getStringProperty("session_type", null) != null && !StringUtils.equals("premium", getStringProperty("session_type", null))) {
            // session_type rapidgator
            return AccountType.FREE;
        } else {
            return AccountType.PREMIUM;
        }
    }

}
