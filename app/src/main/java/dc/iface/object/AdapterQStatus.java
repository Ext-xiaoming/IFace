package dc.iface.object;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import dc.iface.R;


public class AdapterQStatus extends  RecyclerView.Adapter<AdapterQStatus.ViewHolder>  {
    private List<QStatusItem> QStatusData;
    private Context mContext;
    private int resourceId;

    public AdapterQStatus(Context context, int resourceId, List<QStatusItem> data){
        this.mContext = context;
        this.QStatusData = data;
        this.resourceId = resourceId;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        View view;
        TextView qiandaoName;
        TextView jobNumber;
        TextView status;

        public ViewHolder(@NonNull View itemView) {
            super( itemView );
            view=itemView;
            qiandaoName = itemView.findViewById( R.id.name);
            jobNumber = itemView.findViewById(R.id.jobNumber );
            status = itemView.findViewById(R.id.status);
        }
    }

    //定义点击事件的接口
    private OnitemClick onitemClick;   //定义点击事件接口
    private OnLongClick onLongClick;  //定义长按事件接口

    //定义设置点击事件监听的方法
    public void setOnItemClickListener (OnitemClick onitemClick) {
        this.onitemClick = onitemClick;
    }
    //定义设置长按事件监听的方法
    public void setOnLongClickListener (OnLongClick onLongClick) {
        this.onLongClick = onLongClick;
    }

    //定义一个点击事件的接口
    public interface OnitemClick {
        void onItemClick(int position);
    }
    //定义一个长按事件的接口
    public interface OnLongClick {
        void onLongClick(int position);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View  view = LayoutInflater.from(mContext).inflate(resourceId , parent , false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        QStatusItem QStatusDataPoint = QStatusData.get(position);
        holder.jobNumber.setText( "学号:"+ QStatusDataPoint.getStudentId());
        holder.qiandaoName.setText( "姓名:"+  QStatusDataPoint.getName());//int
        //if(QStatusDataPoint.getCheckStatu() == 1)
        holder.status.setText(QStatusDataPoint.getCheckStatu());

        if (onitemClick != null) {
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //在TextView的地方进行监听点击事件，并且实现接口
                    onitemClick.onItemClick(position);
                }
            });
        }

        if (onLongClick != null) {
            holder.view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //在TextView的地方进行长按事件的监听，并实现长按接口
                    onLongClick.onLongClick(position);
                    return true;
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return QStatusData.size();
    }

}
