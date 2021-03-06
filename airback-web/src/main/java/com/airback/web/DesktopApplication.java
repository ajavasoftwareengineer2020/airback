/**
 * Copyright © airback
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.airback.web;

import com.google.common.eventbus.Subscribe;
import com.airback.common.i18n.ErrorI18nEnum;
import com.airback.common.i18n.GenericI18Enum;
import com.airback.common.i18n.ShellI18nEnum;
import com.airback.configuration.EnDecryptHelper;
import com.airback.configuration.ServerConfiguration;
import com.airback.configuration.SiteConfiguration;
import com.airback.core.*;
import com.airback.i18n.LocalizationHelper;
import com.airback.module.billing.UsageExceedBillingPlanException;
import com.airback.module.user.dao.UserAccountMapper;
import com.airback.module.user.domain.SimpleBillingAccount;
import com.airback.module.user.domain.SimpleUser;
import com.airback.module.user.domain.UserAccount;
import com.airback.module.user.domain.UserAccountExample;
import com.airback.module.user.service.BillingAccountService;
import com.airback.module.user.service.UserService;
import com.airback.shell.event.ShellEvent;
import com.airback.shell.view.*;
import com.airback.spring.AppContextUtil;
import com.airback.vaadin.AppUI;
import com.airback.vaadin.AsyncInvoker;
import com.airback.vaadin.EventBusFactory;
import com.airback.vaadin.UserUIContext;
import com.airback.vaadin.mvp.ControllerRegistry;
import com.airback.vaadin.mvp.PresenterResolver;
import com.airback.vaadin.ui.NotificationUtil;
import com.airback.vaadin.ui.UIUtils;
import com.airback.vaadin.web.ui.ConfirmDialogExt;
import com.airback.vaadin.web.ui.service.BroadcastReceiverService;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.Viewport;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.BrowserWindowOpener;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.UncategorizedSQLException;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.viritin.util.BrowserCookie;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import static com.airback.core.utils.ExceptionUtils.getExceptionType;

/**
 * @author airback Ltd.
 * @since 1.0
 */
@Theme(Version.THEME_VERSION)
@Widgetset("com.airback.widgetset.airbackWidgetSet")
@SpringUI
@Viewport("width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no")
@Title("airback - Online project management")
public class DesktopApplication extends AppUI {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(DesktopApplication.class);

    public static final String ACCOUNT_COOKIE = "airback";
    public static final String TEMP_ACCOUNT_COOKIE = "temp_account_airback";

    private static List<String> ipLists = new ArrayList<>();

    private MainWindowContainer mainWindowContainer;
    private BroadcastReceiverService broadcastReceiverService;

    @Override
    protected void init(VaadinRequest request) {
        broadcastReceiverService = AppContextUtil.getSpringBean(BroadcastReceiverService.class);

        ServerConfiguration serverConfiguration = AppContextUtil.getSpringBean(ServerConfiguration.class);
        if (serverConfiguration.isPush()) {
            getPushConfiguration().setPushMode(PushMode.MANUAL);
        }

        UI.getCurrent().setErrorHandler(new DefaultErrorHandler() {
            private static final long serialVersionUID = 1L;

            @Override
            public void error(com.vaadin.server.ErrorEvent event) {
                Throwable e = event.getThrowable();
                handleException(request, e);
            }
        });

        setCurrentFragmentUrl(this.getPage().getUriFragment());
        setCurrentContext(new UserUIContext());
        postSetupApp(request);

        EventBusFactory.getInstance().register(new ShellErrorHandler());

        mainWindowContainer = new MainWindowContainer();
        this.setContent(mainWindowContainer);

        getPage().addPopStateListener((Page.PopStateListener) event -> enter(event.getPage().getUriFragment()));

        String userAgent = request.getHeader("user-agent");
        if (isInNotSupportedBrowserList(userAgent.toLowerCase())) {
            NotificationUtil.showWarningNotification(UserUIContext.getMessage(ErrorI18nEnum.BROWSER_OUT_UP_DATE));
        }
    }

    @Override
    protected void refresh(VaadinRequest request) {
        EventBusFactory.getInstance().post(new ShellEvent.RefreshPage(this));
    }

    private boolean isInNotSupportedBrowserList(String userAgent) {
        return (userAgent.contains("msie 6.0")) || (userAgent.contains("msie 6.1"))
                || userAgent.contains("msie 7.0") || userAgent.contains("msie 8.0") || userAgent.contains("msie 9.0");
    }

    private static Class[] systemExceptions = new Class[]{UncategorizedSQLException.class};

    private String printRequest(VaadinRequest request) {
        StringBuilder requestInfo = new StringBuilder();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String attr = headerNames.nextElement();
            requestInfo.append(attr).append(": ").append(request.getHeader(attr)).append('\n');
        }
        requestInfo.append("Subdomain: ").append(UIUtils.getSubDomain(request)).append('\n');
        requestInfo.append("Remote address: ").append(request.getRemoteAddr()).append('\n');
        requestInfo.append("Path info: ").append(request.getPathInfo()).append('\n');
        requestInfo.append("Remote smtphost: ").append(request.getRemoteHost()).append('\n');
        return requestInfo.toString();
    }

    private void handleException(VaadinRequest request, Throwable e) {
        IgnoreException ignoreException = getExceptionType(e, IgnoreException.class);
        if (ignoreException != null) {
            LOG.error("Exception should not happen", ignoreException);
            return;
        }

        DebugException debugException = getExceptionType(e, DebugException.class);
        if (debugException != null) {
            LOG.error("Debug error", e);
            return;
        }

        SessionExpireException sessionExpireException = getExceptionType(e, SessionExpireException.class);
        if (sessionExpireException != null) {
            UIUtils.reloadPage();
            return;
        }

        UsageExceedBillingPlanException usageBillingException = getExceptionType(e, UsageExceedBillingPlanException.class);
        if (usageBillingException != null) {
            if (UserUIContext.isAdmin()) {
                ConfirmDialogExt.show(UI.getCurrent(),
                        UserUIContext.getMessage(GenericI18Enum.WINDOW_ATTENTION_TITLE, AppUI.getSiteName()),
                        UserUIContext.getMessage(GenericI18Enum.EXCEED_BILLING_PLAN_MSG_FOR_ADMIN),
                        UserUIContext.getMessage(GenericI18Enum.ACTION_YES),
                        UserUIContext.getMessage(GenericI18Enum.ACTION_NO),
                        confirmDialog -> {
                            if (confirmDialog.isConfirmed()) {
                                Collection<Window> windows = UI.getCurrent().getWindows();
                                windows.forEach(Window::close);
                                EventBusFactory.getInstance().post(new ShellEvent.GotoUserAccountModule(this, new String[]{"billing"}));
                            }
                        });

            } else {
                NotificationUtil.showErrorNotification(UserUIContext.getMessage(GenericI18Enum.EXCEED_BILLING_PLAN_MSG_FOR_USER));
            }
            return;
        }

        UserInvalidInputException invalidException = getExceptionType(e, UserInvalidInputException.class);
        if (invalidException != null) {
            NotificationUtil.showWarningNotification(UserUIContext.getMessage(
                    GenericI18Enum.ERROR_USER_INPUT_MESSAGE, invalidException.getMessage()));
            return;
        }

        UnsupportedFeatureException unsupportedException = getExceptionType(e, UnsupportedFeatureException.class);
        if (unsupportedException != null) {
            NotificationUtil.showFeatureNotPresentInSubscription();
            return;
        }

        ResourceNotFoundException resourceNotFoundException = getExceptionType(e, ResourceNotFoundException.class);
        if (resourceNotFoundException != null) {
            NotificationUtil.showWarningNotification(UserUIContext.getMessage(ErrorI18nEnum.RESOURCE_NOT_FOUND));
            LOG.error("404", resourceNotFoundException);
            return;
        }

        SecureAccessException secureAccessException = getExceptionType(e, SecureAccessException.class);
        if (secureAccessException != null) {
            NotificationUtil.showWarningNotification(UserUIContext.getMessage(ErrorI18nEnum.NO_ACCESS_PERMISSION));
            EventBusFactory.getInstance().post(new ShellEvent.GotoUserAccountModule(this, new String[]{"preview"}));
            return;
        }

        for (Class systemEx : systemExceptions) {
            Exception ex = (Exception) getExceptionType(e, systemEx);
            if (ex != null) {
                ConfirmDialog dialog = ConfirmDialogExt.show(DesktopApplication.this,
                        UserUIContext.getMessage(GenericI18Enum.WINDOW_ERROR_TITLE, AppUI.getSiteName()),
                        UserUIContext.getMessage(GenericI18Enum.ERROR_USER_SYSTEM_ERROR, ex.getMessage()),
                        UserUIContext.getMessage(GenericI18Enum.ACTION_YES),
                        UserUIContext.getMessage(GenericI18Enum.ACTION_NO),
                        confirmDialog -> {
                        });
                Button okBtn = dialog.getOkButton();
                BrowserWindowOpener opener = new BrowserWindowOpener("https://support.airback.com");
                opener.extend(okBtn);
                return;
            }
        }

        IllegalStateException asyncNotSupport = getExceptionType(e, IllegalStateException.class);
        if (asyncNotSupport != null && asyncNotSupport.getMessage().contains("!asyncSupported")) {
            ConfirmDialog dialog = ConfirmDialogExt.show(DesktopApplication.this,
                    UserUIContext.getMessage(GenericI18Enum.WINDOW_ERROR_TITLE, AppUI.getSiteName()),
                    UserUIContext.getMessage(ErrorI18nEnum.WEBSOCKET_NOT_SUPPORT),
                    UserUIContext.getMessage(GenericI18Enum.ACTION_YES),
                    UserUIContext.getMessage(GenericI18Enum.ACTION_NO),
                    confirmDialog -> {
                    });
            Button okBtn = dialog.getOkButton();
            BrowserWindowOpener opener = new BrowserWindowOpener("https://support.airback.com");
            opener.extend(okBtn);
            if (request != null) {
                String remoteAddress = request.getRemoteHost();
                if (remoteAddress != null) {
                    if (!ipLists.contains(remoteAddress)) {
                        LOG.error("Async not supported: " + printRequest(request));
                        ipLists.add(remoteAddress);
                    }
                }
            }
            return;
        }

        LOG.error("Error", e);
        ConfirmDialog dialog = ConfirmDialogExt.show(DesktopApplication.this,
                UserUIContext.getMessage(GenericI18Enum.WINDOW_ERROR_TITLE, AppUI.getSiteName()),
                UserUIContext.getMessage(GenericI18Enum.ERROR_USER_NOTICE_INFORMATION_MESSAGE),
                UserUIContext.getMessage(GenericI18Enum.ACTION_YES),
                UserUIContext.getMessage(GenericI18Enum.ACTION_NO),
                confirmDialog -> {
                });
        Button okBtn = dialog.getOkButton();
        BrowserWindowOpener opener = new BrowserWindowOpener("https://support.airback.com");
        opener.extend(okBtn);
    }

    private void enter(String newFragmentUrl) {
        ShellUrlResolver.ROOT.resolveFragment(newFragmentUrl);
    }

    private void clearSession() {
        if (getCurrentContext() != null) {
            getCurrentContext().clearSessionVariables();
            setCurrentFragmentUrl("");
        }
        Broadcaster.unregister(broadcastReceiverService);
    }

    @Override
    public void detach() {
        Broadcaster.unregister(broadcastReceiverService);
        super.detach();
    }

    public void doLogin(String username, String password, boolean isRememberPassword) {
        UserService userService = AppContextUtil.getSpringBean(UserService.class);
        SimpleUser user = userService.authentication(username, password, AppUI.getSubDomain(), false);

        if (isRememberPassword) {
            rememberAccount(username, password);
        } else {
            rememberTempAccount(username, password);
        }

        afterDoLogin(user);
    }

    public void afterDoLogin(SimpleUser user) {
        BillingAccountService billingAccountService = AppContextUtil.getSpringBean(BillingAccountService.class);

        SimpleBillingAccount billingAccount = billingAccountService.getBillingAccountById(AppUI.getAccountId());
        LOG.info(String.format("Get billing account successfully - Pricing: %s, Account: %d, User: %s - %s", "" + billingAccount.getBillingPlan().getPricing(),
                billingAccount.getId(), user.getUsername(), user.getDisplayName()));
        UserUIContext.getInstance().setSessionVariables(user, billingAccount);

        UserAccountMapper userAccountMapper = AppContextUtil.getSpringBean(UserAccountMapper.class);
        UserAccount userAccount = new UserAccount();
        userAccount.setLastaccessedtime(LocalDateTime.now());
        UserAccountExample ex = new UserAccountExample();
        ex.createCriteria().andAccountidEqualTo(billingAccount.getId()).andUsernameEqualTo(user.getUsername());
        userAccountMapper.updateByExampleSelective(userAccount, ex);
        EventBusFactory.getInstance().post(new ShellEvent.GotoMainPage(this, null));
        broadcastReceiverService.registerApp(this);
        Broadcaster.register(broadcastReceiverService);
    }

    public void redirectToLoginView() {
        clearSession();

        AppUI.addFragment("", LocalizationHelper.getMessage(SiteConfiguration.getDefaultLocale(), ShellI18nEnum.OPT_LOGIN_PAGE));
        // clear cookie remember username/password if any
        this.unsetRememberPassword();

        ControllerRegistry.addController(new ShellController(mainWindowContainer));
        LoginPresenter presenter = PresenterResolver.getPresenter(LoginPresenter.class);
        LoginView loginView = presenter.getView();

        mainWindowContainer.setStyleName("loginView");

        if (loginView.getParent() == null || loginView.getParent() == mainWindowContainer) {
            mainWindowContainer.setContent(loginView);
        } else {
            presenter.go(mainWindowContainer, null);
        }
    }

    private void rememberAccount(String username, String password) {
        String storeVal = username + "$" + EnDecryptHelper.encryptText(password);
        BrowserCookie.setCookie(ACCOUNT_COOKIE, storeVal);
    }

    private void rememberTempAccount(String username, String password) {
        String storeVal = username + "$" + EnDecryptHelper.encryptText(password);
        String setCookieVal = String.format("var now = new Date(); now.setTime(now.getTime() + 1 * 1800 * 1000); " +
                "document.cookie = \"%s=%s; expires=\" + now.toUTCString() + \"; path=/\";", TEMP_ACCOUNT_COOKIE, storeVal);
        JavaScript.getCurrent().execute(setCookieVal);
    }

    private void unsetRememberPassword() {
        BrowserCookie.setCookie(ACCOUNT_COOKIE, "");
        BrowserCookie.setCookie(TEMP_ACCOUNT_COOKIE, "");
    }

    public UserUIContext getAssociateContext() {
        return (UserUIContext) getAttribute("context");
    }

    private class ShellErrorHandler {
        @Subscribe
        public void handle(ShellEvent.NotifyErrorEvent event) {
            Throwable e = (Throwable) event.getData();
            AsyncInvoker.access(getUI(), new AsyncInvoker.PageCommand() {
                @Override
                public void run() {
                    handleException(null, e);
                }
            });
        }
    }
}
