/*
 * Copyright (c) 2016.  Jahir Fiquitiva
 *
 * Licensed under the CreativeCommons Attribution-ShareAlike
 * 4.0 International License. You may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *    http://creativecommons.org/licenses/by-sa/4.0/legalcode
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Big thanks to the project contributors. Check them in the repository.
 *
 */

/*
 *
 */

package jahirfiquitiva.iconshowcase.tasks;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.models.IconItem;
import jahirfiquitiva.iconshowcase.models.IconsCategory;
import jahirfiquitiva.iconshowcase.models.IconsLists;
import jahirfiquitiva.iconshowcase.utilities.Utils;


public class LoadIconsLists extends AsyncTask<Void, String, Boolean> {

    private final WeakReference<Context> context;
    private static ArrayList<IconsLists> iconsLists;
    private static ArrayList<IconsCategory> categories;
    private long startTime, endTime;

    public LoadIconsLists(Context context) {
        this.context = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute() {
        startTime = System.currentTimeMillis();
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        boolean worked;

        Resources r = context.get().getResources();
        String p = context.get().getPackageName();

        int iconResId;

        iconsLists = new ArrayList<>();

        String[] prev = r.getStringArray(R.array.preview);
        List<String> previewIconsL = sortList(prev);

        ArrayList<IconItem> previewIconsArray = new ArrayList<>();
        for (String icon : previewIconsL) {
            iconResId = Utils.getIconResId(r, p, icon);
            if (iconResId != 0) {
                previewIconsArray.add(new IconItem(icon, iconResId));
            }
        }
        iconsLists.add(new IconsLists(previewIconsArray));

        String[] tabsNames = r.getStringArray(R.array.tabs);
        categories = new ArrayList<>();
        ArrayList<IconItem> allIcons = new ArrayList<>();

        for (String tabName : tabsNames) {
            int arrayId = r.getIdentifier(tabName, "array", p);
            String[] icons = null;

            try {
                icons = r.getStringArray(arrayId);
            } catch (Resources.NotFoundException e) {
                Utils.showLog(context.get(), "Couldn't find array: " + tabName);
            }

            if (icons != null && icons.length > 0) {
                List<String> iconsList = sortList(icons);

                ArrayList<IconItem> iconsArray = new ArrayList<>();

                for (int j = 0; j < iconsList.size(); j++) {
                    iconResId = Utils.getIconResId(r, p, iconsList.get(j));
                    if (iconResId != 0) {
                        iconsArray.add(new IconItem(iconsList.get(j), iconResId));
                        if (context.get().getResources().getBoolean(R.bool.auto_generate_all_icons)) {
                            allIcons.add(new IconItem(iconsList.get(j), iconResId));
                        }
                    }
                }

                categories.add(new IconsCategory(Utils.makeTextReadable(tabName), iconsArray));
            }
        }

        if (context.get().getResources().getBoolean(R.bool.auto_generate_all_icons)) {
            ArrayList<IconItem> allTheIcons = getAllIconsList(r, p, allIcons);
            if (allTheIcons.size() > 0) {
                categories.add(new IconsCategory("All", allTheIcons));
                worked = true;
            } else {
                worked = false;
            }
        } else {
            String[] allIconsArray = r.getStringArray(R.array.icon_pack);
            if (allIconsArray.length > 0) {
                categories.add(new IconsCategory("All", sortAndOrganizeList(r, p, allIconsArray)));
                worked = true;
            } else {
                worked = false;
            }
        }
        endTime = System.currentTimeMillis();
        return worked;
    }

    @Override
    protected void onPostExecute(Boolean worked) {
        if (worked) {
            Utils.showLog(context.get(),
                    "Load of icons task completed successfully in: " +
                            String.valueOf((endTime - startTime)) + " millisecs.");
        }
    }

    private List<String> sortList(String[] array) {
        List<String> list = new ArrayList<>(Arrays.asList(array));
        Collections.sort(list);
        return list;
    }

    private ArrayList<IconItem> sortAndOrganizeList(Resources r, String p, String[] array) {

        List<String> list = sortList(array);

        Set<String> noDuplicates = new HashSet<>();
        noDuplicates.addAll(list);
        list.clear();
        list.addAll(noDuplicates);
        Collections.sort(list);

        ArrayList<IconItem> sortedListArray = new ArrayList<>();

        for (int j = 0; j < list.size(); j++) {
            int resId = Utils.getIconResId(r, p, list.get(j));
            if (resId != 0) {
                sortedListArray.add(new IconItem(list.get(j), resId));
            }
        }

        return sortedListArray;
    }

    private ArrayList<IconItem> getAllIconsList(Resources r, String p,
                                                ArrayList<IconItem> initialList) {

        String[] allIconsNames = new String[initialList.size()];

        for (int i = 0; i < initialList.size(); i++) {
            allIconsNames[i] = initialList.get(i).getName();
        }

        return sortAndOrganizeList(r, p, allIconsNames);
    }

    public static ArrayList<IconsLists> getIconsLists() {
        return iconsLists.size() > 0 ? iconsLists : null;
    }

    public static ArrayList<IconsCategory> getIconsCategories() {
        return categories.size() > 0 ? categories : null;
    }

}