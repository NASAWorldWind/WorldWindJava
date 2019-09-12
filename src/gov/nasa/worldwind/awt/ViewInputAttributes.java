/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.awt;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.util.Logging;

import java.awt.event.*;
import java.util.*;

/**
 * @author dcollins
 * @version $Id: ViewInputAttributes.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class ViewInputAttributes
{
    public class DeviceModifierMap extends HashMap<Object, ArrayList>
    {
    }

    public class ActionAttributesList extends ArrayList<ActionAttributes>
    {
    }

    public static class ActionAttributes
    {
        public KeyInputActionHandler getActionListener()
        {
            return actionListener;
        }

        public void setActionListener(KeyInputActionHandler actionListener)
        {
            this.actionListener = actionListener;
        }

        public MouseInputActionHandler getMouseActionListener()
        {
            return mouseActionListener;
        }

        public void setMouseActionListener(MouseInputActionHandler mouseActionListener)
        {
            this.mouseActionListener = mouseActionListener;
        }

        public enum ActionTrigger
        {
            ON_PRESS,
            ON_DRAG,
            ON_KEY_DOWN,
            ON_RELEASE
        }

        public static class KeyAction
        {
            public static final int KA_DIR_X = 0;
            public static final int KA_DIR_Y = 1;
            public static final int KA_DIR_Z = 2;

            public int keyCode;
            public int sign;
            public int direction;

            public KeyAction(int key, int direction, int sign)
            {
                this.keyCode = key;
                this.sign = sign;
                this.direction = direction;
            }
        }

        public static class MouseAction
        {
            public int mouseButton;

            public MouseAction(int mouseButton)
            {
                this.mouseButton = mouseButton;
            }
        }

        public static final int NO_MODIFIER = 0;
        private double minValue;
        private double maxValue;
        private boolean enableSmoothing;
        private double smoothingValue;
        private int keyCodeModifier;
        private java.util.List keyActions;
        private java.util.List mouseActions;
        private ActionTrigger actionTrigger;
        private KeyInputActionHandler actionListener;
        private MouseInputActionHandler mouseActionListener;

        public ActionAttributes(ActionAttributes.KeyAction[] keyActions, ActionTrigger trigger,
            int modifier, double minValue, double maxValue,
            boolean enableSmoothing, double smoothingValue)
        {
            this.setValues(minValue, maxValue);
            this.setEnableSmoothing(enableSmoothing);
            this.setSmoothingValue(smoothingValue);
            this.setKeyActions(keyActions);
            this.setKeyCodeModifier(modifier);
            this.setActionTrigger(trigger);
            mouseActions = null;
        }

        public ActionAttributes(ActionAttributes.MouseAction[] mouseActions, ActionTrigger trigger,
            double minValue, double maxValue, boolean enableSmoothing, double smoothingValue)
        {
            this.setValues(minValue, maxValue);
            this.setEnableSmoothing(enableSmoothing);
            this.setSmoothingValue(smoothingValue);
            this.setMouseActions(mouseActions);
            this.setActionTrigger(trigger);
            keyActions = null;
        }

        public ActionAttributes(double minValue, double maxValue, boolean enableSmoothing, double smoothingValue)
        {
            this.setValues(minValue, maxValue);
            this.setEnableSmoothing(enableSmoothing);
            this.setSmoothingValue(smoothingValue);
        }

        public ActionAttributes(ActionAttributes attributes)
        {
            if (attributes == null)
            {
                String message = Logging.getMessage("nullValue.AttributesIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.minValue = attributes.minValue;
            this.maxValue = attributes.maxValue;
            this.smoothingValue = attributes.smoothingValue;
            this.setActionListener(attributes.getActionListener());
            this.setKeyActions(attributes.getKeyActions());
            this.setActionTrigger(attributes.getActionTrigger());
        }

        public double[] getValues()
        {
            return new double[] {this.minValue, this.maxValue};
        }

        public void setValues(double minValue, double maxValue)
        {
            if (minValue <= 0)
            {
                String message = Logging.getMessage("generic.ArgumentOutOfRange", "minValue <= 0");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }
            if (maxValue <= 0)
            {
                String message = Logging.getMessage("generic.ArgumentOutOfRange", "maxValue <= 0");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.minValue = minValue;
            this.maxValue = maxValue;
        }

        public void setValue(double value)
        {
            this.setValues(value, value);
        }

        public boolean isEnableSmoothing()
        {
            return this.enableSmoothing;
        }

        public void setEnableSmoothing(boolean enable)
        {
            this.enableSmoothing = enable;
        }

        public double getSmoothingValue()
        {
            return this.smoothingValue;
        }

        public void setSmoothingValue(double smoothingValue)
        {
            if (smoothingValue < 0 || smoothingValue >= 1.0)
            {
                String message = Logging.getMessage("generic.ArgumentOutOfRange",
                    "smoothingValue < 0 || smoothingValue >= 1");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.smoothingValue = smoothingValue;
        }

        public void setKeyCodeModifier(int modifier)
        {
            this.keyCodeModifier = modifier;
        }

        public int getKeyCodeModifier()
        {
            return (this.keyCodeModifier);
        }

        public java.util.List getKeyActions()
        {
            return (this.keyActions);
        }

        public void setKeyActions(KeyAction[] keyActions)
        {
            this.keyActions = Arrays.asList(keyActions);
        }

        public void setKeyActions(java.util.List keyActions)
        {
            this.keyActions = keyActions;
        }

        public java.util.List getMouseActions()
        {
            return (this.mouseActions);
        }

        public void setMouseActions(MouseAction[] mouseActions)
        {
            this.mouseActions = Arrays.asList(mouseActions);
        }

        public void setMouseActions(java.util.List mouseActions)
        {
            this.mouseActions = mouseActions;
        }

        public ActionTrigger getActionTrigger()
        {
            return this.actionTrigger;
        }

        public void setActionTrigger(ActionTrigger actionTrigger)
        {
            this.actionTrigger = actionTrigger;
        }

        public static ActionAttributes.MouseAction createMouseActionAttribute(int mouseButton)
        {
            ActionAttributes.MouseAction mouseAction = new ActionAttributes.MouseAction(mouseButton);
            return (mouseAction);
        }
    }

    public static class DeviceAttributes
    {
        private double sensitivity;

        public DeviceAttributes(double sensitivity)
        {
            this.setSensitivity(sensitivity);
        }

        public DeviceAttributes(DeviceAttributes attributes)
        {
            if (attributes == null)
            {
                String message = Logging.getMessage("nullValue.AttributesIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.sensitivity = attributes.sensitivity;
        }

        public double getSensitivity()
        {
            return this.sensitivity;
        }

        public void setSensitivity(double sensitivity)
        {
            if (sensitivity <= 0)
            {
                String message = Logging.getMessage("generic.ArgumentOutOfRange", "sensitivity <= 0");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.sensitivity = sensitivity;
        }
    }

    public static class ActionAttributesMap
    {
        private Map<Object, ActionAttributes> actionMap = new HashMap<Object, ActionAttributes>();

        public ActionAttributesMap()
        {
        }

        public ActionAttributes getActionAttributes(Object actionKey)
        {
            if (actionKey == null)
            {
                String message = Logging.getMessage("nullValue.ActionKeyIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            return this.actionMap.get(actionKey);
        }

        public void setActionAttributes(Object actionKey, ActionAttributes attributes)
        {
            if (actionKey == null)
            {
                String message = Logging.getMessage("nullValue.ActionKeyIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }
            if (attributes == null)
            {
                String message = Logging.getMessage("nullValue.AttributesIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.actionMap.put(actionKey, attributes);
        }
    }

    public static final String VIEW_FOCUS = "gov.nasa.worldwind.ViewFocus";
    public static final String VIEW_FOCUS_SLOW = "gov.nasa.worldwind.ViewFocusSlow";
    public static final String VIEW_PAN = "gov.nasa.worldwind.ViewPan";
    public static final String VIEW_PAN_SLOW = "gov.nasa.worldwind.ViewPanSlow";
    public static final String VIEW_ROTATE = "gov.nasa.worldwind.ViewRotate";
    public static final String VIEW_ROTATE_SHIFT = "gov.nasa.worldwind.ViewRotateShift";
    public static final String VIEW_ROTATE_SLOW = "gov.nasa.worldwind.ViewRotateSlow";
    public static final String VIEW_ZOOM = "gov.nasa.worldwind.ViewZoom";
    public static final String VIEW_ZOOM_SLOW = "gov.nasa.worldwind.ViewZoomSlow";
    public static final String DEVICE_KEYBOARD = "gov.nasa.worldwind.DeviceKeyboard";
    public static final String DEVICE_MOUSE = "gov.nasa.worldwind.DeviceMouse";
    public static final String DEVICE_MOUSE_WHEEL = "gov.nasa.worldwind.DeviceMouseWheel";

    public static final String DEVICE_KEYBOARD_MODS = "gov.nasa.worldwind.DeviceKeyboardMods";
    public static final String DEVICE_MOUSE_MODS = "gov.nasa.worldwind.DeviceMouseMods";
    public static final String DEVICE_MOUSE_WHEEL_MODS = "gov.nasa.worldwind.DeviceMouseWheelMods";

    // Action keys
    public static final String VIEW_MOVE_TO = "gov.nasa.worldwind.ViewMoveTo";
    public static final String VIEW_MOVE_TO_SLOW = "gov.nasa.worldwind.MoveToSlow";
    public static final String VIEW_HORIZONTAL_TRANSLATE = "gov.nasa.worldwind.ViewHorizTrans";
    public static final String VIEW_HORIZONTAL_TRANSLATE_SLOW = "gov.nasa.worldwind.ViewHorizTransSlow";
    public static final String VIEW_VERTICAL_TRANSLATE = "gov.nasa.worldwind.ViewVertTrans";
    public static final String VIEW_VERTICAL_TRANSLATE_CTRL = "gov.nasa.worldwind.ViewVertTransCtrl";
    public static final String VIEW_VERTICAL_TRANSLATE_SLOW = "gov.nasa.worldwind.ViewVertTransSlow";
    public static final String VIEW_RESET_HEADING = "gov.nasa.worldwind.ViewResetHeading";
    public static final String VIEW_RESET_HEADING_PITCH_ROLL = "gov.nasa.worldwind.ViewResetHeadingPitchRoll";
    public static final String VIEW_STOP_VIEW = "gov.nasa.worldwind.ViewStopView";

    // Action names for extensible view/navigation system
    public static final String VIEW_HORIZONTAL_TRANS_KEYS = "gov.nasa.worldwind.ViewHorizTransKeys";
    public static final String VIEW_VERTICAL_TRANS_KEYS_META = "gov.nasa.worldwind.ViewVertTransKeysMeta";
    public static final String VIEW_VERTICAL_TRANS_KEYS_CTRL = "gov.nasa.worldwind.ViewVertTransKeysCTRL";
    public static final String VIEW_VERTICAL_TRANS_KEYS = "gov.nasa.worldwind.ViewVertTransKeys";
    public static final String VIEW_VERTICAL_TRANS_KEYS_SLOW_META = "gov.nasa.worldwind.ViewVertTransKeysMetaSlow";
    public static final String VIEW_VERTICAL_TRANS_KEYS_SLOW_CTRL = "gov.nasa.worldwind.ViewVertTransKeysCtrlSlow";
    public static final String VIEW_VERTICAL_TRANS_KEYS_SLOW = "gov.nasa.worldwind.ViewVertTransKeysSlow";
    public static final String VIEW_ROTATE_KEYS = "gov.nasa.worldwind.ViewRotateKeys";
    public static final String VIEW_ROTATE_KEYS_SHIFT = "gov.nasa.worldwind.ViewRotateKeysShift";
    public static final String VIEW_ROTATE_KEYS_SHIFT_SLOW = "gov.nasa.worldwind.ViewRotateKeysShiftSlow";
    public static final String VIEW_ROLL_KEYS = "gov.nasa.worldwind.ViewRollKeys";

    // Reset Heading
    private static final ActionAttributes.KeyAction DEFAULT_RESET_HEADING_KEY_ACT =
        new ActionAttributes.KeyAction(KeyEvent.VK_N, ActionAttributes.KeyAction.KA_DIR_X, 1);
    public static final ActionAttributes.KeyAction[] resetHeadingEvents =
        {
            DEFAULT_RESET_HEADING_KEY_ACT
        };
    // Reset Heading, Pitch, and Roll
    private static final ActionAttributes.KeyAction DEFAULT_RESET_HEADING_PITCH_ROLL_KEY_ACT =
        new ActionAttributes.KeyAction(KeyEvent.VK_R, ActionAttributes.KeyAction.KA_DIR_X, 1);
    public static final ActionAttributes.KeyAction[] resetHeadingPitchRollEvents =
        {
            DEFAULT_RESET_HEADING_PITCH_ROLL_KEY_ACT
        };
    // Stop view
    private static final ActionAttributes.KeyAction DEFAULT_STOP_VIEW_KEY_ACT =
        new ActionAttributes.KeyAction(KeyEvent.VK_SPACE, ActionAttributes.KeyAction.KA_DIR_X, 1);
    public static final ActionAttributes.KeyAction[] stopViewEvents =
        {
            DEFAULT_STOP_VIEW_KEY_ACT
        };

    // MoveTo Events
    private static final ActionAttributes.MouseAction DEFAULT_MOVETO_MOUSE_MODS =
        new ActionAttributes.MouseAction(MouseEvent.BUTTON1);
    public static final ActionAttributes.MouseAction[] moveToMouseEvents =
        {
            DEFAULT_MOVETO_MOUSE_MODS
        };

    // Horizontal Translation mouse events
    private static final ActionAttributes.MouseAction DEFAULT_HORIZONTAL_TRANSLATE_MOUSE_MODS =
        new ActionAttributes.MouseAction(MouseEvent.BUTTON1_DOWN_MASK);
    public static final ActionAttributes.MouseAction[] horizontalTransMouseEvents =
        {
            DEFAULT_HORIZONTAL_TRANSLATE_MOUSE_MODS
        };
    // Horizontal Translation keyboard events
    private static final ActionAttributes.KeyAction DEFAULT_HORIZONTAL_TRANSLEFT_KEY_ACT =
        new ActionAttributes.KeyAction(KeyEvent.VK_LEFT, ActionAttributes.KeyAction.KA_DIR_X, -1);
    private static final ActionAttributes.KeyAction DEFAULT_HORIZONTAL_TRANSRIGHT_KEY_ACT =
        new ActionAttributes.KeyAction(KeyEvent.VK_RIGHT, ActionAttributes.KeyAction.KA_DIR_X, 1);
    private static final ActionAttributes.KeyAction DEFAULT_HORIZONTAL_TRANSUP_KEY_ACT =
        new ActionAttributes.KeyAction(KeyEvent.VK_UP, ActionAttributes.KeyAction.KA_DIR_Y, 1);
    private static final ActionAttributes.KeyAction DEFAULT_HORIZONTAL_TRANSDOWN_KEY_ACT =
        new ActionAttributes.KeyAction(KeyEvent.VK_DOWN, ActionAttributes.KeyAction.KA_DIR_Y, -1);
    public static final ActionAttributes.KeyAction[] horizontalTransKeyEvents =
        {
            DEFAULT_HORIZONTAL_TRANSLEFT_KEY_ACT,
            DEFAULT_HORIZONTAL_TRANSRIGHT_KEY_ACT,
            DEFAULT_HORIZONTAL_TRANSUP_KEY_ACT,
            DEFAULT_HORIZONTAL_TRANSDOWN_KEY_ACT
        };

    // Vertical Translation Mouse Events
    private static final ActionAttributes.MouseAction DEFAULT_VERTICAL_TRANSLATE_MOUSE_MODS =
        new ActionAttributes.MouseAction(MouseEvent.BUTTON2_DOWN_MASK);
    public static final ActionAttributes.MouseAction[] verticalTransMouseEvents =
        {
            DEFAULT_VERTICAL_TRANSLATE_MOUSE_MODS
        };
    private static final ActionAttributes.MouseAction DEFAULT_VERTICAL_TRANSLATE_MOUSE_MODS_CTRL =
        new ActionAttributes.MouseAction(MouseEvent.BUTTON1_DOWN_MASK);
    public static final ActionAttributes.MouseAction[] verticalTransMouseEventsCtrl =
        {
            DEFAULT_VERTICAL_TRANSLATE_MOUSE_MODS_CTRL
        };
    // Vertical Translation Key Events
    private static final ActionAttributes.KeyAction DEFAULT_VERTICAL_TRANSUP_KEY_ACT =
        new ActionAttributes.KeyAction(KeyEvent.VK_UP,
            ActionAttributes.KeyAction.KA_DIR_Z, -1);
    private static final ActionAttributes.KeyAction DEFAULT_VERTICAL_TRANSDOWN_KEY_ACT =
        new ActionAttributes.KeyAction(KeyEvent.VK_DOWN,
            ActionAttributes.KeyAction.KA_DIR_Z, 1);
    public static final ActionAttributes.KeyAction[] verticalTransKeyEventsCtrl =
        {
            DEFAULT_VERTICAL_TRANSUP_KEY_ACT,
            DEFAULT_VERTICAL_TRANSDOWN_KEY_ACT
        };

    private static final ActionAttributes.KeyAction DEFAULT_VERTICAL_TRANSUP_ADDKEY_ACT =
        new ActionAttributes.KeyAction(KeyEvent.VK_ADD,
            ActionAttributes.KeyAction.KA_DIR_Z, -1);
    private static final ActionAttributes.KeyAction DEFAULT_VERTICAL_TRANSDOWN_EQUALSKEY_ACT =
        new ActionAttributes.KeyAction(KeyEvent.VK_EQUALS,
            ActionAttributes.KeyAction.KA_DIR_Z, -1);
    private static final ActionAttributes.KeyAction DEFAULT_VERTICAL_TRANSUP_SUBTKEY_ACT =
        new ActionAttributes.KeyAction(KeyEvent.VK_SUBTRACT,
            ActionAttributes.KeyAction.KA_DIR_Z, 1);
    private static final ActionAttributes.KeyAction DEFAULT_VERTICAL_TRANSDOWN_MINUSKEY_ACT =
        new ActionAttributes.KeyAction(KeyEvent.VK_MINUS,
            ActionAttributes.KeyAction.KA_DIR_Z, 1);
    public static final ActionAttributes.KeyAction[] verticalTransKeyEvents =
        {
            DEFAULT_VERTICAL_TRANSUP_ADDKEY_ACT,
            DEFAULT_VERTICAL_TRANSDOWN_EQUALSKEY_ACT,
            DEFAULT_VERTICAL_TRANSUP_SUBTKEY_ACT,
            DEFAULT_VERTICAL_TRANSDOWN_MINUSKEY_ACT
        };
    private static final ActionAttributes.MouseAction DEFAULT_VERTICAL_TRANSLATE_MOUSE_WHEEL_MODS =
        new ActionAttributes.MouseAction(MouseEvent.MOUSE_WHEEL);
    public static final ActionAttributes.MouseAction[] verticalTransMouseWheelEvents =
        {
            DEFAULT_VERTICAL_TRANSLATE_MOUSE_WHEEL_MODS
        };

    private static final ActionAttributes.MouseAction DEFAULT_ROTATE_MOUSE_MODS =
        new ActionAttributes.MouseAction(MouseEvent.BUTTON3_DOWN_MASK);

    public static final ActionAttributes.MouseAction[] rotateMouseEvents =
        {
            DEFAULT_ROTATE_MOUSE_MODS
        };
    private static final ActionAttributes.MouseAction DEFAULT_ROTATE_MOUSE_MODS_SHIFT =
        new ActionAttributes.MouseAction(MouseEvent.BUTTON1_DOWN_MASK);
    public static final ActionAttributes.MouseAction[] rotateMouseEventsShift =
        {
            DEFAULT_ROTATE_MOUSE_MODS_SHIFT
        };
    // Rotation Keyboard events
    private static final ActionAttributes.KeyAction DEFAULT_ROTATE_HEADINGLEFT_KEY_ACT =
        new ActionAttributes.KeyAction(KeyEvent.VK_LEFT,
            ActionAttributes.KeyAction.KA_DIR_X, -1);
    private static final ActionAttributes.KeyAction DEFAULT_ROTATE_HEADINGRIGHT_KEY_ACT =
        new ActionAttributes.KeyAction(KeyEvent.VK_RIGHT,
            ActionAttributes.KeyAction.KA_DIR_X, 1);
    private static final ActionAttributes.KeyAction DEFAULT_ROTATE_PITCHUP_KEY_ACT =
        new ActionAttributes.KeyAction(KeyEvent.VK_UP,
            ActionAttributes.KeyAction.KA_DIR_Y, -1);
    private static final ActionAttributes.KeyAction DEFAULT_ROTATE_PITCHDOWN_KEY_ACT =
        new ActionAttributes.KeyAction(KeyEvent.VK_DOWN,
            ActionAttributes.KeyAction.KA_DIR_Y, 1);
    public static final ActionAttributes.KeyAction[] rotationKeyEvents =
        {
            DEFAULT_ROTATE_HEADINGLEFT_KEY_ACT,
            DEFAULT_ROTATE_HEADINGRIGHT_KEY_ACT,
            DEFAULT_ROTATE_PITCHUP_KEY_ACT,
            DEFAULT_ROTATE_PITCHDOWN_KEY_ACT
        };

    // Roll Keyboard events. Use CTRL-Left and CTRL-Right to change roll. 
    protected static final ActionAttributes.KeyAction DEFAULT_ROTATE_ROLLUP_KEY_ACT =
        new ActionAttributes.KeyAction(KeyEvent.VK_LEFT,
            ActionAttributes.KeyAction.KA_DIR_Y, 1);
    protected static final ActionAttributes.KeyAction DEFAULT_ROTATE_ROLLDOWN_KEY_ACT =
        new ActionAttributes.KeyAction(KeyEvent.VK_RIGHT,
            ActionAttributes.KeyAction.KA_DIR_Y, -1);
    public static final ActionAttributes.KeyAction[] rollKeyEvents =
        {
            DEFAULT_ROTATE_ROLLUP_KEY_ACT,
            DEFAULT_ROTATE_ROLLDOWN_KEY_ACT
        };

    private static final ActionAttributes.KeyAction DEFAULT_ROTATE_PITCHUP_KEY_PAGE_ACT =
        new ActionAttributes.KeyAction(KeyEvent.VK_PAGE_UP,
            ActionAttributes.KeyAction.KA_DIR_Y, -1);
    private static final ActionAttributes.KeyAction DEFAULT_ROTATE_PITCHDOWN_KEY_PAGE_ACT =
        new ActionAttributes.KeyAction(KeyEvent.VK_PAGE_DOWN,
            ActionAttributes.KeyAction.KA_DIR_Y, 1);
    public static final ActionAttributes.KeyAction[] rotationKeyEventsPage =
        {
            DEFAULT_ROTATE_PITCHUP_KEY_PAGE_ACT,
            DEFAULT_ROTATE_PITCHDOWN_KEY_PAGE_ACT
        };

    public static final boolean DEFAULT_MOVE_TO_SMOOTHING_ENABLED = true;
    public static final boolean DEFAULT_HORIZONTAL_TRANSLATE_SMOOTHING_ENABLED = true;
    public static final boolean DEFAULT_VERTICAL_TRANSLATE_SMOOTHING_ENABLED = true;
    public static final double DEFAULT_MOVE_TO_SMOOTHING_VALUE = 0.0; // [0, 1] smoothing value
    public static final double DEFAULT_HORIZONTAL_TRANSLATE_SMOOTHING_VALUE = 0.4; // [0, 1] smoothing value
    public static final double DEFAULT_VERTICAL_TRANSLATE_SMOOTHING_VALUE = 0.85; // [0, 1] smoothing value
    // Keyboard/Action calibration values for extensible view/navigation support
    public static final double DEFAULT_KEY_HORIZONTAL_TRANSLATE_MIN_VALUE = 0.000005; // Speed in degrees per frame
    public static final double DEFAULT_KEY_HORIZONTAL_TRANSLATE_MAX_VALUE = 4.0; // Speed in degrees per frame
    public static final double DEFAULT_KEY_VERTICAL_TRANSLATE_VALUE = 0.06; // Speed in log-meters per frame
    // Mouse/Action calibration values for extensible view/navigation support
    public static final double DEFAULT_MOUSE_MOVE_TO_MIN_VALUE = 0.95; // [0, 1] smoothing value
    public static final double DEFAULT_MOUSE_MOVE_TO_MAX_VALUE = 0.90; // [0, 1] smoothing value
    public static final double DEFAULT_MOUSE_HORIZONTAL_TRANSLATE_MIN_VALUE = 0.00001;
        // Speed in degrees per mouse movement
    public static final double DEFAULT_MOUSE_HORIZONTAL_TRANSLATE_MAX_VALUE = 0.2;
        // Speed in degrees per mouse movement
    public static final double DEFAULT_MOUSE_VERTICAL_TRANSLATE_VALUE = 0.003; // Speed in log-meters per mouse movement
    // MouseWheel/Action calibration values.
    public static final double DEFAULT_MOUSE_WHEEL_VERTICAL_TRANSLATE_VALUE = 0.1;
        // Speed in log-meters per wheel movement
    public static final double DEFAULT_MOUSE_WHEEL_VERTICAL_TRANSLATE_VALUE_OSX = 0.01;
        // Speed in log-meters per wheel movement

    // Device sensitivity defaults.
    public static final double DEFAULT_KEY_SENSITIVITY = 1.0; // Scalar multiplier
    public static final double DEFAULT_MOUSE_SENSITIVITY = 1.0; // Scalar multiplier
    public static final double DEFAULT_MOUSE_WHEEL_SENSITIVITY = 1.0; // Scalar multiplier
    public static final double DEFAULT_SLOW_VALUE = 0.25; // Scalar multiplier

    public static final boolean DEFAULT_ROTATE_SMOOTHING_ENABLED = true;
    public static final double DEFAULT_ROTATE_SMOOTHING_VALUE = 0.7; // [0, 1] smoothing value
    public static final boolean DEFAULT_ROLL_SMOOTHING_ENABLED = true;
    public static final double DEFAULT_ROLL_SMOOTHING_VALUE = 0.7; // [0, 1] smoothing value

    // Keyboard/Action calibration values.
    public static final double DEFAULT_KEY_ROTATE_MIN_VALUE = 2.0; // Speed in degrees per frame
    public static final double DEFAULT_KEY_ROTATE_MAX_VALUE = 2.2; // Speed in degrees per frame
    public static final double DEFAULT_KEY_ROLL_MIN_VALUE = 2.0; // Speed in degrees per frame
    public static final double DEFAULT_KEY_ROLL_MAX_VALUE = 2.2; // Speed in degrees per frame
    public static final double DEFAULT_MOUSE_ROTATE_MIN_VALUE = 0.14; // Speed in degrees per mouse movement
    public static final double DEFAULT_MOUSE_ROTATE_MAX_VALUE = 0.18; // Speed in degrees per mouse movement

    // Device attributes.
    private Map<Object, DeviceAttributes> deviceMap = new HashMap<Object, DeviceAttributes>();
    // Device/action pairing attributes.
    private Map<Object, ActionAttributesMap> deviceActionMap = new HashMap<Object, ActionAttributesMap>();
    // Device/Modifier/Action map
    // Devices are mapped to modifier keys, which are then mapped to actions.  Actions contain the keys
    // that they are interested in, and only act on those keys.
    private Map<Object, DeviceModifierMap> deviceModActionMap = new HashMap<Object, DeviceModifierMap>();

    public ViewInputAttributes()
    {

        this.setDefaultDeviceAttributes();
        this.setDeviceModifierActionMaps();
    }

    public ActionAttributesMap getActionMap(Object deviceKey)
    {
        if (deviceKey == null)
        {
            String message = Logging.getMessage("nullValue.DeviceKeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.deviceActionMap.get(deviceKey);
    }

    public void setActionMap(Object deviceKey, ActionAttributesMap map)
    {
        if (deviceKey == null)
        {
            String message = Logging.getMessage("nullValue.DeviceKeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (map == null)
        {
            String message = Logging.getMessage("nullValue.MapIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.deviceActionMap.put(deviceKey, map);
    }

    public void addModifierAction(Object device, Integer modifier, ActionAttributes action)
    {
        this.addModifierActionList(device, modifier);
        DeviceModifierMap modActionMap = this.getModifierActionMap(device);

        ActionAttributesList actionList = (ActionAttributesList) modActionMap.get(modifier);
        actionList.remove(action);
        actionList.add(action);
    }

    public void setValues(Object device, Object action, double minValue, double maxValue)
    {
        ActionAttributes actionAttrs = getActionAttributes(device, action);
        if (actionAttrs == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        else
        {
            actionAttrs.setValues(minValue, maxValue);
        }
    }

    public void setActionTrigger(Object device, Object action, ActionAttributes.ActionTrigger trigger)
    {
        ActionAttributes actionAttrs = getActionAttributes(device, action);
        if (actionAttrs == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        else
        {
            actionAttrs.setActionTrigger(trigger);
        }
    }

    public void addModifierActionList(Object device, Integer modifier)
    {
        DeviceModifierMap deviceActionMap = this.getModifierActionMap(device);
        if (deviceActionMap == null)
        {
            deviceActionMap = new DeviceModifierMap();
            this.setModifierActionMap(device, deviceActionMap);
        }
        ArrayList modifierList = deviceActionMap.get(modifier);
        if (modifierList == null)
        {
            deviceActionMap.put(modifier, new ActionAttributesList());
        }
    }

    public List getModifierActionList(Object device, Integer modifier)
    {
        Map<Object, ArrayList> deviceModActionMap = this.getModifierActionMap(device);
        if (deviceModActionMap == null)
        {
            String message = Logging.getMessage("nullValue.DeviceKeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        return (deviceModActionMap.get(modifier));
    }

    public DeviceAttributes getDeviceAttributes(Object deviceKey)
    {
        if (deviceKey == null)
        {
            String message = Logging.getMessage("nullValue.DeviceKeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.deviceMap.get(deviceKey);
    }

    public void setDeviceAttributes(Object deviceKey, DeviceAttributes attributes)
    {
        if (deviceKey == null)
        {
            String message = Logging.getMessage("nullValue.DeviceKeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (attributes == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.deviceMap.put(deviceKey, attributes);
    }

    public DeviceModifierMap getModifierActionMap(Object deviceKey)
    {
        if (deviceKey == null)
        {
            String message = Logging.getMessage("nullValue.DeviceKeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.deviceModActionMap.get(deviceKey);
    }

    public void setModifierActionMap(Object deviceKey, DeviceModifierMap map)
    {
        if (deviceKey == null)
        {
            String message = Logging.getMessage("nullValue.DeviceKeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (map == null)
        {
            String message = Logging.getMessage("nullValue.MapIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.deviceModActionMap.put(deviceKey, map);
    }

    public ActionAttributes getActionAttributes(Object deviceKey, Object actionKey)
    {
        if (deviceKey == null)
        {
            String message = Logging.getMessage("nullValue.DeviceKeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (actionKey == null)
        {
            String message = Logging.getMessage("nullValue.ActionKeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        ActionAttributesMap map = this.getActionMap(deviceKey);
        if (map == null)
            return null;

        return map.getActionAttributes(actionKey);
    }

    public void addAction(Object deviceKey, Integer modifier, Object actionKey,
        ActionAttributes actionAttrs)
    {
        if (deviceKey == null)
        {
            String message = Logging.getMessage("nullValue.DeviceKeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (actionKey == null)
        {
            String message = Logging.getMessage("nullValue.ActionKeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        // Add this action to the Device -> Modifier -> Action map
        addModifierAction(deviceKey, modifier, actionAttrs);

        // Get the Device -> Action map
        ActionAttributesMap deviceActionMap = this.getActionMap(deviceKey);
        if (deviceActionMap == null)
        {
            deviceActionMap = new ActionAttributesMap();
            this.setActionMap(deviceKey, deviceActionMap);
        }

        deviceActionMap.setActionAttributes(actionKey, actionAttrs);
    }

    public void setMouseActionAttributes(String actionName, int modifier, ActionAttributes.ActionTrigger trigger,
        ActionAttributes.MouseAction[] mouseActions,
        double minValue, double maxValue, boolean smoothingEnabled, double smoothingValue)
    {
        ActionAttributes actionAttrs = this.getActionAttributes(DEVICE_MOUSE, actionName);
        if (actionAttrs != null)
        {
            actionAttrs.setValues(minValue, maxValue);
            actionAttrs.setMouseActions(mouseActions);
            actionAttrs.setActionTrigger(trigger);
            actionAttrs.setEnableSmoothing(smoothingEnabled);
            actionAttrs.setSmoothingValue(smoothingValue);
        }
        else
        {
            this.addAction(DEVICE_MOUSE, modifier, actionName,
                new ActionAttributes(mouseActions, trigger,
                    minValue, maxValue,
                    smoothingEnabled, smoothingValue));
        }
    }

    public void setActionListener(Object deviceKey, Object actionKey, ViewInputActionHandler listener)
    {
        if (deviceKey == null)
        {
            String message = Logging.getMessage("nullValue.DeviceKeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (actionKey == null)
        {
            String message = Logging.getMessage("nullValue.ActionKeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        ActionAttributesMap deviceActionMap = this.getActionMap(deviceKey);
        if (deviceActionMap == null)
        {
            String message = Logging.getMessage("nullValue.DeviceNotDefined");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        ActionAttributes actions = deviceActionMap.getActionAttributes(actionKey);
        if (actions == null)
        {
            String message = Logging.getMessage("nullValue.DeviceActionNotDefined");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (actions.getMouseActions() != null)
        {
            actions.setMouseActionListener(listener);
        }
        else if (actions.getKeyActions() != null)
        {
            actions.setActionListener(listener);
        }
    }

    //**************************************************************//
    //********************  Default Attributes  ********************//
    //**************************************************************//

    protected void setDefaultDeviceAttributes()
    {
        this.setDeviceAttributes(DEVICE_KEYBOARD, new DeviceAttributes(DEFAULT_KEY_SENSITIVITY));
        this.setDeviceAttributes(DEVICE_MOUSE, new DeviceAttributes(DEFAULT_MOUSE_SENSITIVITY));
        this.setDeviceAttributes(DEVICE_MOUSE_WHEEL, new DeviceAttributes(DEFAULT_MOUSE_WHEEL_SENSITIVITY));
    }

    protected void setDeviceModifierActionMaps()
    {
        // Mouse Wheel Vertical Translation Event
        if (Configuration.isMacOS())
        {
            this.addAction(DEVICE_MOUSE_WHEEL, ActionAttributes.NO_MODIFIER, VIEW_VERTICAL_TRANSLATE,
                new ActionAttributes(verticalTransMouseWheelEvents, ActionAttributes.ActionTrigger.ON_DRAG,
                    DEFAULT_MOUSE_WHEEL_VERTICAL_TRANSLATE_VALUE_OSX, DEFAULT_MOUSE_WHEEL_VERTICAL_TRANSLATE_VALUE_OSX,
                    DEFAULT_VERTICAL_TRANSLATE_SMOOTHING_ENABLED, DEFAULT_VERTICAL_TRANSLATE_SMOOTHING_VALUE));
        }
        else
        {
            this.addAction(DEVICE_MOUSE_WHEEL, ActionAttributes.NO_MODIFIER, VIEW_VERTICAL_TRANSLATE,
                new ActionAttributes(verticalTransMouseWheelEvents, ActionAttributes.ActionTrigger.ON_DRAG,
                    DEFAULT_MOUSE_WHEEL_VERTICAL_TRANSLATE_VALUE, DEFAULT_MOUSE_WHEEL_VERTICAL_TRANSLATE_VALUE,
                    DEFAULT_VERTICAL_TRANSLATE_SMOOTHING_ENABLED, DEFAULT_VERTICAL_TRANSLATE_SMOOTHING_VALUE));
        }

        // Mouse Button Move To Events
        this.addAction(DEVICE_MOUSE, ActionAttributes.NO_MODIFIER, VIEW_MOVE_TO,
            new ActionAttributes(moveToMouseEvents, ActionAttributes.ActionTrigger.ON_PRESS,
                DEFAULT_MOUSE_MOVE_TO_MIN_VALUE, DEFAULT_MOUSE_MOVE_TO_MAX_VALUE,
                DEFAULT_MOVE_TO_SMOOTHING_ENABLED, DEFAULT_MOVE_TO_SMOOTHING_VALUE));
        this.addAction(DEVICE_MOUSE, KeyEvent.ALT_DOWN_MASK, VIEW_MOVE_TO_SLOW,
            this.makeSlowActionAttributes(this.getActionAttributes(
                DEVICE_MOUSE, VIEW_MOVE_TO), DEFAULT_SLOW_VALUE));

        // Mouse Button Rotate Events
        this.addAction(DEVICE_MOUSE, ActionAttributes.NO_MODIFIER, VIEW_ROTATE,
            new ActionAttributes(rotateMouseEvents, ActionAttributes.ActionTrigger.ON_DRAG,
                DEFAULT_MOUSE_ROTATE_MIN_VALUE, DEFAULT_MOUSE_ROTATE_MAX_VALUE,
                DEFAULT_ROTATE_SMOOTHING_ENABLED, DEFAULT_ROTATE_SMOOTHING_VALUE));
        this.addAction(DEVICE_MOUSE, KeyEvent.SHIFT_DOWN_MASK, VIEW_ROTATE_SHIFT,
            new ActionAttributes(rotateMouseEventsShift, ActionAttributes.ActionTrigger.ON_DRAG,
                DEFAULT_MOUSE_ROTATE_MIN_VALUE, DEFAULT_MOUSE_ROTATE_MAX_VALUE,
                DEFAULT_ROTATE_SMOOTHING_ENABLED, DEFAULT_ROTATE_SMOOTHING_VALUE));

        // Mouse Button Horizontal Translate Events
        this.addAction(DEVICE_MOUSE, ActionAttributes.NO_MODIFIER, VIEW_HORIZONTAL_TRANSLATE,
            new ActionAttributes(horizontalTransMouseEvents, ActionAttributes.ActionTrigger.ON_DRAG,
                DEFAULT_MOUSE_HORIZONTAL_TRANSLATE_MIN_VALUE, DEFAULT_MOUSE_HORIZONTAL_TRANSLATE_MAX_VALUE,
                DEFAULT_HORIZONTAL_TRANSLATE_SMOOTHING_ENABLED, DEFAULT_HORIZONTAL_TRANSLATE_SMOOTHING_VALUE));

        // Mouse Button Vertical Translate Events
        this.addAction(DEVICE_MOUSE, ActionAttributes.NO_MODIFIER, VIEW_VERTICAL_TRANSLATE,
            new ActionAttributes(verticalTransMouseEvents, ActionAttributes.ActionTrigger.ON_DRAG,
                DEFAULT_MOUSE_VERTICAL_TRANSLATE_VALUE, DEFAULT_MOUSE_VERTICAL_TRANSLATE_VALUE,
                DEFAULT_VERTICAL_TRANSLATE_SMOOTHING_ENABLED, DEFAULT_VERTICAL_TRANSLATE_SMOOTHING_VALUE));
        this.addAction(DEVICE_MOUSE, KeyEvent.CTRL_DOWN_MASK, VIEW_VERTICAL_TRANSLATE_CTRL,
            new ActionAttributes(verticalTransMouseEventsCtrl, ActionAttributes.ActionTrigger.ON_DRAG,
                DEFAULT_MOUSE_VERTICAL_TRANSLATE_VALUE, DEFAULT_MOUSE_VERTICAL_TRANSLATE_VALUE,
                DEFAULT_VERTICAL_TRANSLATE_SMOOTHING_ENABLED, DEFAULT_VERTICAL_TRANSLATE_SMOOTHING_VALUE));
        this.addAction(DEVICE_MOUSE, KeyEvent.META_DOWN_MASK, VIEW_VERTICAL_TRANSLATE_CTRL,
            new ActionAttributes(verticalTransMouseEventsCtrl, ActionAttributes.ActionTrigger.ON_DRAG,
                DEFAULT_MOUSE_VERTICAL_TRANSLATE_VALUE, DEFAULT_MOUSE_VERTICAL_TRANSLATE_VALUE,
                DEFAULT_VERTICAL_TRANSLATE_SMOOTHING_ENABLED, DEFAULT_VERTICAL_TRANSLATE_SMOOTHING_VALUE));

        // Keyboard Rotation Actions
        this.addAction(DEVICE_KEYBOARD, KeyEvent.SHIFT_DOWN_MASK, VIEW_ROTATE_KEYS_SHIFT,
            new ActionAttributes(rotationKeyEvents, ActionAttributes.ActionTrigger.ON_KEY_DOWN,
                KeyEvent.SHIFT_DOWN_MASK,
                DEFAULT_KEY_ROTATE_MIN_VALUE, DEFAULT_KEY_ROTATE_MAX_VALUE,
                DEFAULT_ROTATE_SMOOTHING_ENABLED, DEFAULT_ROTATE_SMOOTHING_VALUE));

        this.addAction(DEVICE_KEYBOARD, ActionAttributes.NO_MODIFIER, VIEW_ROTATE_KEYS,
            new ActionAttributes(rotationKeyEventsPage, ActionAttributes.ActionTrigger.ON_KEY_DOWN,
                ActionAttributes.NO_MODIFIER,
                DEFAULT_KEY_ROTATE_MIN_VALUE, DEFAULT_KEY_ROTATE_MAX_VALUE,
                DEFAULT_ROTATE_SMOOTHING_ENABLED, DEFAULT_ROTATE_SMOOTHING_VALUE));

        this.addAction(DEVICE_KEYBOARD, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.ALT_DOWN_MASK, VIEW_ROTATE_KEYS_SHIFT_SLOW,
            this.makeSlowActionAttributes(this.getActionAttributes(
                DEVICE_KEYBOARD, VIEW_ROTATE_KEYS_SHIFT), DEFAULT_SLOW_VALUE));
        this.addAction(DEVICE_KEYBOARD, KeyEvent.ALT_DOWN_MASK, VIEW_ROTATE_SLOW,
            this.makeSlowActionAttributes(this.getActionAttributes(
                DEVICE_KEYBOARD, VIEW_ROTATE_KEYS), DEFAULT_SLOW_VALUE));

        // Keyboard Roll Actions
        this.addAction(DEVICE_KEYBOARD, KeyEvent.CTRL_DOWN_MASK, VIEW_ROLL_KEYS,
            new ActionAttributes(rollKeyEvents, ActionAttributes.ActionTrigger.ON_KEY_DOWN,
                KeyEvent.CTRL_DOWN_MASK,
                DEFAULT_KEY_ROLL_MIN_VALUE, DEFAULT_KEY_ROLL_MAX_VALUE,
                DEFAULT_ROLL_SMOOTHING_ENABLED, DEFAULT_ROLL_SMOOTHING_VALUE));

        // Keyboard Horizontal Translation Actions
        this.addAction(DEVICE_KEYBOARD, ActionAttributes.NO_MODIFIER, VIEW_HORIZONTAL_TRANS_KEYS,
            new ActionAttributes(horizontalTransKeyEvents, ActionAttributes.ActionTrigger.ON_KEY_DOWN, 0,
                DEFAULT_KEY_HORIZONTAL_TRANSLATE_MIN_VALUE, DEFAULT_KEY_HORIZONTAL_TRANSLATE_MAX_VALUE,
                DEFAULT_HORIZONTAL_TRANSLATE_SMOOTHING_ENABLED, DEFAULT_HORIZONTAL_TRANSLATE_SMOOTHING_VALUE));
        this.addAction(DEVICE_KEYBOARD, KeyEvent.ALT_DOWN_MASK, VIEW_HORIZONTAL_TRANSLATE_SLOW,
            this.makeSlowActionAttributes(this.getActionAttributes(
                DEVICE_KEYBOARD, VIEW_HORIZONTAL_TRANS_KEYS), DEFAULT_SLOW_VALUE));

        // Vertical Translation Actions
        this.addAction(DEVICE_KEYBOARD, ActionAttributes.NO_MODIFIER, VIEW_VERTICAL_TRANS_KEYS,
            new ActionAttributes(verticalTransKeyEvents, ActionAttributes.ActionTrigger.ON_KEY_DOWN, 0,
                DEFAULT_KEY_VERTICAL_TRANSLATE_VALUE, DEFAULT_KEY_VERTICAL_TRANSLATE_VALUE,
                DEFAULT_VERTICAL_TRANSLATE_SMOOTHING_ENABLED, DEFAULT_VERTICAL_TRANSLATE_SMOOTHING_VALUE));
        this.addAction(DEVICE_KEYBOARD, KeyEvent.ALT_DOWN_MASK, VIEW_VERTICAL_TRANS_KEYS_SLOW,
            this.makeSlowActionAttributes(this.getActionAttributes(
                DEVICE_KEYBOARD, VIEW_VERTICAL_TRANS_KEYS), DEFAULT_SLOW_VALUE));

        this.addAction(DEVICE_KEYBOARD, KeyEvent.CTRL_DOWN_MASK, VIEW_VERTICAL_TRANS_KEYS_CTRL,
            new ActionAttributes(verticalTransKeyEventsCtrl, ActionAttributes.ActionTrigger.ON_KEY_DOWN,
                (KeyEvent.CTRL_DOWN_MASK),
                DEFAULT_KEY_VERTICAL_TRANSLATE_VALUE, DEFAULT_KEY_VERTICAL_TRANSLATE_VALUE,
                DEFAULT_VERTICAL_TRANSLATE_SMOOTHING_ENABLED, DEFAULT_VERTICAL_TRANSLATE_SMOOTHING_VALUE));
        this.addAction(DEVICE_KEYBOARD, KeyEvent.META_DOWN_MASK, VIEW_VERTICAL_TRANS_KEYS_META,
            this.getActionAttributes(DEVICE_KEYBOARD, VIEW_VERTICAL_TRANS_KEYS));
        this.addAction(DEVICE_KEYBOARD, KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK,
            VIEW_VERTICAL_TRANS_KEYS_SLOW_CTRL,
            this.makeSlowActionAttributes(this.getActionAttributes(
                DEVICE_KEYBOARD, VIEW_VERTICAL_TRANS_KEYS_CTRL), DEFAULT_SLOW_VALUE));
        this.addAction(DEVICE_KEYBOARD, KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK,
            VIEW_VERTICAL_TRANS_KEYS_SLOW_META,
            this.makeSlowActionAttributes(this.getActionAttributes(
                DEVICE_KEYBOARD, VIEW_VERTICAL_TRANS_KEYS_CTRL), DEFAULT_SLOW_VALUE));

        // Reset Heading Action
        this.addAction(DEVICE_KEYBOARD, ActionAttributes.NO_MODIFIER, VIEW_RESET_HEADING,
            new ActionAttributes(resetHeadingEvents, ActionAttributes.ActionTrigger.ON_PRESS, 0,
                DEFAULT_KEY_ROTATE_MIN_VALUE, DEFAULT_KEY_ROTATE_MAX_VALUE,
                DEFAULT_ROTATE_SMOOTHING_ENABLED, DEFAULT_ROTATE_SMOOTHING_VALUE));
        // Reset Heading, Pitch, and Roll Action
        this.addAction(DEVICE_KEYBOARD, ActionAttributes.NO_MODIFIER, VIEW_RESET_HEADING_PITCH_ROLL,
            new ActionAttributes(resetHeadingPitchRollEvents, ActionAttributes.ActionTrigger.ON_PRESS, 0,
                DEFAULT_KEY_ROTATE_MIN_VALUE, DEFAULT_KEY_ROTATE_MAX_VALUE,
                DEFAULT_ROTATE_SMOOTHING_ENABLED, DEFAULT_ROTATE_SMOOTHING_VALUE));
        // Stop View Action
        this.addAction(DEVICE_KEYBOARD, ActionAttributes.NO_MODIFIER, VIEW_STOP_VIEW,
            new ActionAttributes(stopViewEvents, ActionAttributes.ActionTrigger.ON_PRESS, 0,
                0.1, 0.1, false, 0.1));
    }

    protected ActionAttributes makeSlowActionAttributes(ActionAttributes attributes, double slowCoefficient)
    {
        ActionAttributes slowAttributes = new ActionAttributes(attributes);
        double[] values = attributes.getValues();
        slowAttributes.setValues(values[0] * slowCoefficient, values[1] * slowCoefficient);
        slowAttributes.setEnableSmoothing(attributes.isEnableSmoothing());
        slowAttributes.setSmoothingValue(attributes.getSmoothingValue());
        slowAttributes.setKeyCodeModifier(attributes.getKeyCodeModifier());
        slowAttributes.setKeyActions(attributes.getKeyActions());
        return slowAttributes;
    }
}
