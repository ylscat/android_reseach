package arbell.research.ui.view;

import java.util.List;

public class TreeNode
{
    public String name;
    public int level;
    public List<TreeNode> data;

    @Override
    public String toString()
    {
        return name;
    }
}