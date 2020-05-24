package dc.iface.object;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;



import java.util.List;

import dc.iface.R;

public class AdapterQStatus extends ArrayAdapter<QStatusItem> {
    private List<QStatusItem> QStatusData;
    private Context mContext;
    private int resourceId;

    public AdapterQStatus(Context context, int resourceId, List<QStatusItem> data){
        super(context,resourceId,data);
        this.mContext = context;
        this.QStatusData = data;
        this.resourceId = resourceId;
    }

    @Override
    public View getView(int position , View convertView , ViewGroup parent) {
        QStatusItem QStatusDataPoint = getItem(position);
        View view = LayoutInflater.from( getContext() ). inflate(resourceId , parent , false );

        TextView qiandaoName = view.findViewById( R.id.name);
        TextView jobNumber = view.findViewById(R.id.jobNumber );
        TextView status = view.findViewById(R.id.status);

        jobNumber.setText( "学号:"+ QStatusDataPoint.getStudentId());
        qiandaoName.setText( "姓名:"+  QStatusDataPoint.getName());//int
        //if(QStatusDataPoint.getCheckStatu() == 1)
        status.setText(QStatusDataPoint.getCheckStatu());
        return view;
    }
}
