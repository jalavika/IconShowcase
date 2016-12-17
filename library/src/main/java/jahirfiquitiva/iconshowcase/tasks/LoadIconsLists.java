/*
 * Copyright (c) 2016 Jahir Fiquitiva
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
 * Special thanks to the project contributors and collaborators
 * 	https://github.com/jahirfiquitiva/IconShowcase#special-thanks
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
import jahirfiquitiva.iconshowcase.activities.ShowcaseActivity;
import jahirfiquitiva.iconshowcase.activities.base.DrawerActivity;
import jahirfiquitiva.iconshowcase.fragments.MainFragment;
import jahirfiquitiva.iconshowcase.holders.FullListHolder;
import jahirfiquitiva.iconshowcase.models.IconItem;
import jahirfiquitiva.iconshowcase.models.IconsCategory;
import jahirfiquitiva.iconshowcase.utilities.utils.IconUtils;
import jahirfiquitiva.iconshowcase.utilities.utils.Utils;
import timber.log.Timber;

public class LoadIconsLists extends AsyncTask<Void, String, Boolean> {

    private final WeakReference<Context> mContext;
    private ArrayList<IconItem> mPreviewIcons = new ArrayList<>();
    private ArrayList<IconsCategory> mCategoryList = new ArrayList<>();
    private long startTime, endTime;

    public LoadIconsLists(Context context) {
        mContext = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute() {
        startTime = System.currentTimeMillis();
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        Resources r = mContext.get().getResources();
        String p = mContext.get().getPackageName();

        String[] previews = r.getStringArray(R.array.preview);
        for (String icon : previews) {
            int iconResId = IconUtils.getIconResId(r, p, icon);
            if (iconResId > 0) {
                mPreviewIcons.add(new IconItem(icon, iconResId));
            }
        }

        if (mPreviewIcons.size() > 0) {
            try {
                FullListHolder.get().home().createList(mPreviewIcons);
                if (((ShowcaseActivity) mContext.get()).getCurrentFragment() instanceof
                        MainFragment) {
                    ((ShowcaseActivity) mContext.get()).setupIcons();
                }
            } catch (Exception e) {
                // Do nothing
            }
        }

        String[] tabsNames = r.getStringArray(R.array.tabs);
        ArrayList<IconItem> allIcons = new ArrayList<>();

        for (String tabName : tabsNames) {
            int arrayId = r.getIdentifier(tabName, "array", p);
            String[] icons;
            try {
                icons = r.getStringArray(arrayId);
                if (icons.length > 0) {
                    ArrayList<IconItem> iconsArray = buildIconsListFromArray(r, p, icons);
                    if (mContext.get().getResources().getBoolean(R.bool.auto_generate_all_icons)) {
                        allIcons.addAll(iconsArray);
                    }
                    if (iconsArray.size() > 0) {
                        mCategoryList.add(new IconsCategory(IconUtils.formatName(tabName),
                                sortIconsList(r, p, iconsArray)));
                    }
                }
            } catch (Resources.NotFoundException e) {
                Timber.d("Couldn't find array: " + tabName);
            }
        }

        if (mContext.get().getResources().getBoolean(R.bool.auto_generate_all_icons)) {
            ArrayList<IconItem> allTheIcons = sortIconsList(r, p, allIcons);
            if (allTheIcons.size() > 0) {
                mCategoryList.add(new IconsCategory("All", allTheIcons));
            }
        } else {
            String[] allIconsArray = r.getStringArray(R.array.icon_pack);
            if (allIconsArray.length > 0) {
                mCategoryList.add(
                        new IconsCategory("All",
                                sortIconsList(r, p,
                                        buildIconsListFromArray(r, p, allIconsArray))));
            }
        }

        return (!(mPreviewIcons.isEmpty())) && (!(mCategoryList.isEmpty()));
    }

    @Override
    protected void onPostExecute(Boolean worked) {
        if (worked) {
            FullListHolder.get().home().createList(mPreviewIcons);
            FullListHolder.get().iconsCategories().createList(mCategoryList);
            if (mContext.get() instanceof ShowcaseActivity) {
                if (((ShowcaseActivity) mContext.get()).getCurrentFragment() instanceof
                        MainFragment) {
                    ((MainFragment) ((ShowcaseActivity) mContext.get()).getCurrentFragment())
                            .updateAppInfoData();
                }
                ((ShowcaseActivity) mContext.get()).resetFragment(DrawerActivity.DrawerItem
                        .PREVIEWS);
            }
            Timber.d("Load of icons task completed successfully in: %d milliseconds", (endTime -
                    startTime));
        } else {
            Timber.d("Something went really wrong while loading icons.");
        }
    }

    private ArrayList<IconItem> sortIconsList(Resources r, String p, ArrayList<IconItem> icons) {
        if (r.getBoolean(R.bool.organize_icons_lists)) {
            List<String> list = new ArrayList<>();
            for (IconItem icon : icons) {
                list.add(icon.getName());
            }
            Collections.sort(list);
            Set<String> noDuplicates = new HashSet<>();
            noDuplicates.addAll(list);
            list.clear();
            list.addAll(noDuplicates);
            Collections.sort(list);
            return buildIconsListFromArray(r, p, list);
        } else {
            return icons;
        }
    }

    private ArrayList<IconItem> buildIconsListFromArray(Resources r, String p, List<String>
            icons) {
        ArrayList<IconItem> list = new ArrayList<>();
        for (String iconName : icons) {
            int iconResId = IconUtils.getIconResId(r, p, iconName);
            if (iconResId > 0) {
                list.add(new IconItem(iconName, iconResId));
            } else {
                Timber.d("Icon \'" + iconName + "\' could not be found." +
                        " Make sure you added it in resources.");
            }
        }
        return list;
    }

    private ArrayList<IconItem> buildIconsListFromArray(Resources r, String p, String[] icons) {
        return buildIconsListFromArray(r, p, Arrays.asList(icons));
    }

}