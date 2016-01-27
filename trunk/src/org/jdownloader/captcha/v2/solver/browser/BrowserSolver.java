package org.jdownloader.captcha.v2.solver.browser;

import org.jdownloader.captcha.v2.Challenge;
import org.jdownloader.captcha.v2.challenge.recaptcha.v2.RecaptchaV2Challenge;
import org.jdownloader.captcha.v2.solver.service.BrowserSolverService;
import org.jdownloader.settings.advanced.AdvancedConfigManager;

public class BrowserSolver extends AbstractBrowserSolver {

    private static final BrowserSolver INSTANCE = new BrowserSolver();

    public static BrowserSolver getInstance() {
        return INSTANCE;
    }

    private BrowserSolver() {
        super(1);
        AdvancedConfigManager.getInstance().register(BrowserSolverService.getInstance().getConfig());
    }

    @Override
    public boolean canHandle(Challenge<?> c) {
        if (!validateBlackWhite(c)) {
            return false;
        }
        if (c instanceof RecaptchaV2Challenge) {
            return CFG_BROWSER_CAPTCHA_SOLVER.CFG.isRecaptcha2Enabled();
        }
        if (c instanceof AbstractBrowserChallenge) {

            return true;
        }
        return false;
    }

}
