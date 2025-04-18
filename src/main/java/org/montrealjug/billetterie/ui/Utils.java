package org.montrealjug.billetterie.ui;

import org.montrealjug.billetterie.entity.Activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Utils {


    static List<PresentationActivity> toIndexActivities(Set<Activity> activities) {
        List<PresentationActivity> indexActivities = new ArrayList<>();
        activities.forEach(activity -> {
            PresentationActivity indexActivity = new PresentationActivity(activity.getId(), activity.getTitle(), activity.getDescription(), activity.getMaxParticipants(),activity.getMaxWaitingQueue(), activity.getStartTime().toLocalTime());
            indexActivities.add(indexActivity);
        });
        return indexActivities;

    }

}
