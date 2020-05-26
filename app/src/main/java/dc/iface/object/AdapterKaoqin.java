package dc.iface.object;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dc.iface.R;

public class AdapterKaoqin extends RecyclerView.Adapter<AdapterKaoqin.ViewHolder> {

    private List<ListItemKaoqin> KaoqinData;
    private Context mContext;
    private int resourceId;

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


    public AdapterKaoqin(Context context, int resourceId, List<ListItemKaoqin> data){
        this.mContext = context;
        this.KaoqinData = data;
        this.resourceId = resourceId;
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        View itemView;
        TextView qiandaoTime;
        TextView qiandaoNumber;
        TextView checkNumber;

        public ViewHolder(@NonNull View view) {
            super( view );
            itemView=view;
            qiandaoTime = view.findViewById( R.id. qiandaoTime );
            qiandaoNumber = view.findViewById(R.id.qiandaoNumber );
            checkNumber = view.findViewById(R.id.checkNumber);
        }
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
        ListItemKaoqin kaoqinPoint = KaoqinData.get(position);

        holder.qiandaoTime.setText( "时间:"+kaoqinPoint.getTime());
        holder.qiandaoNumber.setText( kaoqinPoint.getQiandaoNumber() );//签到总人数
        holder.checkNumber.setText("第" +kaoqinPoint.getCheckNumber()+"次签到");

        if (onitemClick != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //在TextView的地方进行监听点击事件，并且实现接口
                    onitemClick.onItemClick(position);
                }
            });
        }

        if (onLongClick != null) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
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
        return KaoqinData.size();
    }
}
