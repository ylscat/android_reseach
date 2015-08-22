package arbell.research;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.LinearLayout;
import arbell.research.ui.view.Navigator;
import arbell.research.ui.view.TreeLeafNode;
import arbell.research.ui.view.TreeNode;

import java.util.ArrayList;
import java.util.HashMap;

public class Catalogue extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navi);
        LinearLayout container = (LinearLayout)findViewById(R.id.container);
        new Navigator(container, buildCatalogue());
    }

    private TreeNode buildCatalogue() {
        ActivityInfo[] infos;
        PackageManager pm = getPackageManager();
        try {
            PackageInfo pkgInfo = pm.getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
            infos = pkgInfo.activities;
        } catch (PackageManager.NameNotFoundException e) {
            //will not happen
            return null;
        }

        String pkgPrefix = getPackageName() + '.';
        String exclude = getComponentName().getClassName();
        HashMap<String, TreeNode> treeMap = new HashMap<String, TreeNode>();
        TreeNode root = new TreeNode();
        root.level = 0;
        root.data = new ArrayList<TreeNode>();
        for(ActivityInfo info : infos) {
            String name = info.name;
            if(exclude.equals(name))
                continue;
            if(name.contains(pkgPrefix))
                name = name.substring(pkgPrefix.length());
            int preIndex = 0;
            int index = name.indexOf('.');
            TreeNode node = root;
            TreeLeafNode leaf = new TreeLeafNode();
            leaf.level = 1;
            while (index != -1) {
                String prefix = name.substring(0, index);
                TreeNode pkg = treeMap.get(prefix);
                if(pkg == null) {
                    pkg = new TreeNode();
                    pkg.level = leaf.level;
                    pkg.data = new ArrayList<TreeNode>();
                    pkg.name = name.substring(preIndex, index);
                    node.data.add(pkg);
                    treeMap.put(prefix, pkg);
                }
                leaf.level++;
                node = pkg;
                preIndex = index + 1;
                index = name.indexOf('.', preIndex);
            }
            if(info.labelRes != 0 || info.nonLocalizedLabel != null)
                leaf.name = info.loadLabel(pm).toString();
            else
                leaf.name = name.substring(preIndex);
            leaf.data = info.name;
            node.data.add(leaf);
        }

        return root;
    }
}
