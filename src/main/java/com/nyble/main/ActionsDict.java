package com.nyble.main;

import com.nyble.topics.Names;
import com.nyble.topics.consumerActions.ConsumerActionsValue;
import com.nyble.types.ConsumerActionDescriptor;

import java.util.*;

public class ActionsDict {
    static Map<String, ConsumerActionDescriptor> actionsDict = new HashMap<>();
    static {
        registerAction(new ConsumerActionDescriptor(1728, Names.RMC_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(1730, Names.RMC_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(1740, Names.RMC_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(1755, Names.RMC_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(1763, Names.RMC_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(1784, Names.RMC_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(1814, Names.RMC_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(1828, Names.RMC_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(1832, Names.RMC_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(1833, Names.RMC_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(1851, Names.RMC_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(1858, Names.RMC_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(1866, Names.RMC_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(1898, Names.RMC_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(1964, Names.RMC_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(2048, Names.RMC_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(2051, Names.RMC_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(2057, Names.RMC_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(2067, Names.RMC_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(2080, Names.RMC_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(2098, Names.RMC_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3016, Names.RMC_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3017, Names.RMC_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3037, Names.RMC_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3064, Names.RMC_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3065, Names.RMC_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3079, Names.RMC_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3081, Names.RMC_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3083, Names.RMC_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3089, Names.RMC_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3090, Names.RMC_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3092, Names.RMC_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3135, Names.RMC_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3147, Names.RMC_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3149, Names.RMC_SYSTEM_ID+""));

        registerAction(new ConsumerActionDescriptor(1730, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(1775, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(1832, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(1851, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(1894, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(1898, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(2080, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3064, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3068, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3069, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3071, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3074, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3103, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3105, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3106, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3107, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3108, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3109, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3110, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3111, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3112, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3113, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3339, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3349, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3350, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3351, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3352, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3353, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3354, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3355, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3356, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3357, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3358, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3359, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3360, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3364, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3365, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3366, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3367, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3368, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3370, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3371, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3372, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3378, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3382, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3383, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3384, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3388, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3389, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3390, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3394, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3395, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3396, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3397, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3400, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3401, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3402, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3418, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3419, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3420, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3424, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3425, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3426, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3430, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3431, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3432, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3437, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3444, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3445, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3446, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3447, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3448, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3449, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3450, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3452, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3453, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3454, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3455, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3456, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3457, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3459, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3460, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3461, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3462, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3464, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3465, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3466, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3473, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3477, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3480, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3481, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3482, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3483, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3484, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3485, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3486, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3488, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3489, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3490, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3491, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3492, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3497, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3498, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3502, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3528, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3529, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3533, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3534, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3539, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3576, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3577, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3578, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3579, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3581, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3582, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3583, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3584, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3585, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3586, Names.RRP_SYSTEM_ID+""));
        registerAction(new ConsumerActionDescriptor(3587, Names.RRP_SYSTEM_ID+""));
    }

    private static void registerAction(ConsumerActionDescriptor cad){
        actionsDict.put(cad.getSystemId()+"#"+cad.getId(), cad);
    }

    public static boolean contains(String systemId, String actionId){
        return actionsDict.containsKey(systemId+"#"+actionId);
    }


    public static boolean filter(ConsumerActionsValue cav){
        String systemId = cav.getSystemId()+"";
        String actionId = cav.getActionId()+"";
        Date actionDate = new Date(Long.parseLong(cav.getExternalSystemDate()));
        Date until = new Date();
        Calendar pastTwoYears = new GregorianCalendar();
        pastTwoYears.add(Calendar.YEAR, -2);
        Date from = pastTwoYears.getTime();
        return contains(systemId, actionId) && actionDate.before(until) && actionDate.after(from);
    }

}
