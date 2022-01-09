import java.awt.Toolkit;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Table
{
    private ArrayList<ColumnProperties> columns = new ArrayList<>();
    private int column = 0;
    private int columnOffset = 0;
    private int tableOffset = 0;

    private static HashMap<Integer, Table> tables = new HashMap<>();

    private Table(int tableOffset, String... columnNames)
    {
        this.tableOffset = tableOffset;
        for(String name : columnNames)
        {
            addColumn(name);
        }

    }

    private static Table createTable(int tableOffset, String... columnNames)
    {
        Table table;
        int hashCode = Arrays.hashCode(columnNames);
        if(tables.containsKey(hashCode))
        {
            table = tables.get(hashCode);
            table.tableOffset = tableOffset;
        }
        else
        {
            table = new Table(tableOffset, columnNames);
            tables.put(hashCode, table);
        }
        table.printHeader();
        return table;
    }

    public static Table createTable(String... columnNames)
    {
        return createTable(++Debug.currentRow, columnNames);
    }

    private void addColumn(int x, String name)
    {
        ColumnProperties columnPorperties = new ColumnProperties();
        columnPorperties.width = name.length();
        columnPorperties.offset = columnOffset;
        columnPorperties.name = name;

        columns.add(columnPorperties);

        columnOffset += name.length() + 2;
    }

    private void addColumn(String name)
    {
        addColumn(column++, name);
    }

    private void printHeader()
    {
        columns.stream().forEach(e -> Debug.printf(e.offset, tableOffset, String.format("%" + e.width + "s", e.name)));
    }

    private static class ColumnProperties
    {
        int width;
        int offset;
        String name;
    }

    public void printColumn(int row, int column, String value)
    {
        ColumnProperties c;
        if((c = columns.get(column)) != null)
        {
            if(value.length() > (c.width))
            {
                int offset = value.length() - c.width;
                c.width = value.length();

                for(int i = column + 1; i < columns.size(); ++i)
                {
                    c = columns.get(i);
                    c.offset += offset;
                }
            }
            Debug.printf(c.offset, row  + tableOffset, "%" + c.width + "s", value);
            Debug.currentRow = Math.max(Debug.currentRow,  row + tableOffset + 2);
        }
    }

    public void printColumn(int row, int column, Integer value)
    {
        printColumn(row, column, value.toString());
    }

    public void printColumn(int row, int column, Double value)
		{
			BigDecimal bd = BigDecimal.valueOf(value);
			bd = bd.setScale(2, RoundingMode.HALF_UP);
			printColumn(row, column, String.valueOf(bd.doubleValue()));
		}
}
