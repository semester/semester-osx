/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package semester.readymade.cf.util;

import semester.readymade.cf.Foundation;
import semester.readymade.cf.ID;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

import static semester.readymade.cf.Foundation.invoke;
import static semester.readymade.cf.Foundation.toStringViaUTF8;

/**
 * @author pegov
 */
public class MacUtil {
  public static final String MAC_NATIVE_WINDOW_SHOWING = "MAC_NATIVE_WINDOW_SHOWING";
  
  private MacUtil() {
  }

  public static ID findWindowForTitle(String title) {
    if (title == null || title.isEmpty()) return null;
    final ID pool = invoke("NSAutoreleasePool", "new");

    ID focusedWindow = null;
    try {
      final ID sharedApplication = invoke("NSApplication", "sharedApplication");
      final ID windows = invoke(sharedApplication, "windows");
      final ID windowEnumerator = invoke(windows, "objectEnumerator");

      while (true) {
        // dirty hack: walks through all the windows to find a cocoa window to show sheet for
        final ID window = invoke(windowEnumerator, "nextObject");
        if (0 == window.intValue()) break;

        final ID windowTitle = invoke(window, "title");
        if (windowTitle != null && windowTitle.intValue() != 0) {
          final String titleString = toStringViaUTF8(windowTitle);
          if (Comparing.equal(titleString, title)) {
            focusedWindow = window;
            break;
          }
        }
      }
    }
    finally {
      invoke(pool, "release");
    }

    return focusedWindow;
  }

  public static synchronized void startModal(JComponent component, String key) {
    try {
      if (SwingUtilities.isEventDispatchThread()) {
        EventQueue theQueue = component.getToolkit().getSystemEventQueue();

        while (component.getClientProperty(key) == Boolean.TRUE) {
          AWTEvent event = theQueue.getNextEvent();
          Object source = event.getSource();
          if (event instanceof ActiveEvent) {
            ((ActiveEvent)event).dispatch();
          }
          else if (source instanceof Component) {
            ((Component)source).dispatchEvent(event);
          }
          else if (source instanceof MenuComponent) {
            ((MenuComponent)source).dispatchEvent(event);
          }
          else {
//            LOG.debug("Unable to dispatch: " + event);
          }
        }
      }
      else {
        assert false: "Should be called from Event-Dispatch Thread only!";
        while (component.getClientProperty(key) == Boolean.TRUE) {
          // TODO:
          //wait();
        }
      }
    }
    catch (InterruptedException ignored) {
    }
  }
  
  public static synchronized void startModal(JComponent component) {
    startModal(component, MAC_NATIVE_WINDOW_SHOWING);
  }

  public static boolean isFullKeyboardAccessEnabled() {
    if (!SystemUtil.isMacOSSnowLeopard) return false;
    final AtomicBoolean result = new AtomicBoolean();
    Foundation.executeOnMainThread(new Runnable() {
        @Override
        public void run() {
            result.set(invoke(invoke("NSApplication", "sharedApplication"), "isFullKeyboardAccessEnabled").intValue() == 1);
        }
    }, true, true);
    return result.get();
  }

//  public static void adjustFocusTraversal(Disposable disposable) {
//    if (!SystemUtil.isMacOSSnowLeopard) return;
//    final AWTEventListener listener = new AWTEventListener() {
//      @Override
//      public void eventDispatched(AWTEvent event) {
//        if (event instanceof KeyEvent
//            && ((KeyEvent)event).getKeyCode() == KeyEvent.VK_TAB
//            && (!(event.getSource() instanceof JTextComponent))
//            && (!(event.getSource() instanceof JList))
//            && !isFullKeyboardAccessEnabled())
//          ((KeyEvent)event).consume();
//      }
//    };
//    Disposer.register(disposable, new Disposable() {
//      @Override
//      public void dispose() {
//        Toolkit.getDefaultToolkit().removeAWTEventListener(listener);
//      }
//    });
//    Toolkit.getDefaultToolkit().addAWTEventListener(listener, AWTEvent.KEY_EVENT_MASK);
//  }

  @SuppressWarnings("deprecation")
  public static ID findWindowFromJavaWindow(final Window w) {
    ID windowId = null;
    if (SystemUtil.isJavaVersionAtLeast("1.7")) {
      try {
        //noinspection deprecation
        Class <?> cWindowPeerClass  = w.getPeer().getClass();
        Method getPlatformWindowMethod = cWindowPeerClass.getDeclaredMethod("getPlatformWindow");
        Object cPlatformWindow = getPlatformWindowMethod.invoke(w.getPeer());
        Class <?> cPlatformWindowClass = cPlatformWindow.getClass();
        Method getNSWindowPtrMethod = cPlatformWindowClass.getDeclaredMethod("getNSWindowPtr");
        windowId = new ID((Long)getNSWindowPtrMethod.invoke(cPlatformWindow));
      }
      catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
//        LOG.debug(e);
      }
    }
    else {
      String foremostWindowTitle = getWindowTitle(w);
      windowId = findWindowForTitle(foremostWindowTitle);
    }
    return windowId;
  }

  public static String getWindowTitle(Window documentRoot) {
    String windowTitle = null;
    if (documentRoot instanceof Frame) {
      windowTitle = ((Frame)documentRoot).getTitle();
    }
    else if (documentRoot instanceof Dialog) {
      windowTitle = ((Dialog)documentRoot).getTitle();
    }
    return windowTitle;
  }
}