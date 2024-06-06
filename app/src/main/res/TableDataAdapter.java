// TableDataAdapter.java
public class TableDataAdapter extends RecyclerView.Adapter<TableDataAdapter.ViewHolder> {

    private List<List<String>> tableData;

    public TableDataAdapter(List<List<String>> tableData) {
        this.tableData = tableData;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_table_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        List<String> rowData = tableData.get(position);
        holder.bindData(rowData);
    }

    @Override
    public int getItemCount() {
        return tableData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private List<TextView> rowTextViews;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rowTextViews = new ArrayList<>();
            LinearLayout linearLayoutRow = itemView.findViewById(R.id.linearLayoutRow);
            for (int i = 0; i < linearLayoutRow.getChildCount(); i++) {
                if (linearLayoutRow.getChildAt(i) instanceof TextView) {
                    rowTextViews.add((TextView) linearLayoutRow.getChildAt(i));
                }
            }
        }

        public void bindData(List<String> rowData) {
            int minSize = Math.min(rowData.size(), rowTextViews.size());
            for (int i = 0; i < minSize; i++) {
                rowTextViews.get(i).setText(rowData.get(i));
            }
        }
    }
}
