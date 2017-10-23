package io.appium.uiautomator2.handler;

import android.support.test.uiautomator.StaleObjectException;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;

import io.appium.uiautomator2.handler.request.SafeRequestHandler;
import io.appium.uiautomator2.http.AppiumResponse;
import io.appium.uiautomator2.http.IHttpRequest;
import io.appium.uiautomator2.model.AndroidElement;
import io.appium.uiautomator2.model.KnownElements;
import io.appium.uiautomator2.server.WDStatus;
import io.appium.uiautomator2.utils.Logger;

public class ScrollToElement extends SafeRequestHandler {

    public ScrollToElement(String mappedUri) {
        super(mappedUri);
    }

    @Override
    public AppiumResponse safeHandle(IHttpRequest request) {
        Logger.info("Scroll into view command");
        String id = getElementId(request);

        AndroidElement element = KnownElements.getElementFromCache(id);
        if (element == null) {
            return new AppiumResponse(getSessionId(request), WDStatus.NO_SUCH_ELEMENT);
        }
        try {
            UiObject uiObject = (UiObject) element.getUiObject();
            UiScrollable uiScrollable = new UiScrollable(new UiSelector().scrollable(true).instance(0));

            boolean flag = uiScrollable.scrollIntoView(uiObject);

            return new AppiumResponse(getSessionId(request), WDStatus.SUCCESS, flag);
        } catch (UiObjectNotFoundException e) {
            Logger.error("Element not found: ", e);
            return new AppiumResponse(getSessionId(request), WDStatus.NO_SUCH_ELEMENT);
        } catch(StaleObjectException e){
            Logger.error("Stale Element Exception: ", e);
            return new AppiumResponse(getSessionId(request), WDStatus.STALE_ELEMENT_REFERENCE, e);
        }
    }

    private class UiScrollable extends android.support.test.uiautomator.UiScrollable {

        /**
         * Constructor.
         *
         * @param container a {@link UiSelector} selector to identify the scrollable
         *                  layout element.
         * @since API Level 16
         */
        public UiScrollable(UiSelector container) {
            super(container);
        }

        @Override
        public boolean scrollIntoView(UiObject obj) throws UiObjectNotFoundException {
            if (obj.exists()) {
                return true;
            } else {
                System.out.println("It doesn't exist on this page");
                // we will need to reset the search from the beginning to start search
                flingToBeginning(getMaxSearchSwipes());
                if (obj.exists()) {
                    return true;
                } else {
                    for (int x = 0; x < getMaxSearchSwipes(); x++) {
                        System.out.println("I'm going forward a page: " + x);
                        if(!scrollForward() && x!=0) { // x!=0 is the hack
                            return false;
                        }

                        if (obj.exists()) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }
}
