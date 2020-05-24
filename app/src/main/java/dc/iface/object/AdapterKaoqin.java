package dc.iface.object;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import java.util.List;

import dc.iface.R;

public class AdapterKaoqin extends ArrayAdapter<ListItemKaoqin> {
    private List<ListItemKaoqin> KaoqinData;
    private Context mContext;
    private int resourceId;

    public AdapterKaoqin(Context context, int resourceId, List<ListItemKaoqin> data){
        super(context,resourceId,data);
        this.mContext = context;
        this.KaoqinData = data;
        this.resourceId = resourceId;
    }

    @Override
    public View getView(int position , View convertView , ViewGroup parent) {
        ListItemKaoqin kaoqinPoint = getItem(position);
        View view = LayoutInflater.from( getContext() ). inflate(resourceId , parent , false );

        TextView qiandaoTime = view.findViewById( R.id. qiandaoTime );
        TextView qiandaoNumber = view.findViewById(R.id.qiandaoNumber );
        TextView checkNumber = view.findViewById(R.id.checkNumber);

        qiandaoTime.setText( "时间:"+kaoqinPoint.getTime());
        qiandaoNumber.setText( kaoqinPoint.getQiandaoNumber() );//签到总人数
        checkNumber.setText("第" +kaoqinPoint.getCheckNumber()+"次签到");
        return view;
    }
}
