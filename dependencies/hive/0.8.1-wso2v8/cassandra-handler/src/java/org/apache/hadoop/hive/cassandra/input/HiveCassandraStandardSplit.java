package org.apache.hadoop.hive.cassandra.input;

import org.apache.cassandra.hadoop.ColumnFamilySplit;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.InputSplit;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

@SuppressWarnings("deprecation")
public class HiveCassandraStandardSplit extends FileSplit implements InputSplit{
  private InputSplit split;
  private String columnMapping;
  private String keyspace;
  private String columnFamily;
  private int rangeBatchSize;
  private int slicePredicateSize;
  private int splitSize;
  private boolean isIncremental;
  //added for 7.0
  private String partitioner;
  private int port;
  private String host;

  public HiveCassandraStandardSplit() {
    super((Path) null, 0, 0, (String[]) null);
    columnMapping = "";
    split  = new ColumnFamilySplit(null,null,null);
  }

  public HiveCassandraStandardSplit(InputSplit split, String columnsMapping, Path dummyPath) {
    super(dummyPath, 0, 0, (String[]) null);
    this.split = split;
    columnMapping = columnsMapping;
    if(split instanceof IncrementalColumnFamilySplit){
        isIncremental = true;
    }
  }

  @Override
  public void readFields(DataInput in) throws IOException {
    super.readFields(in);
    columnMapping = in.readUTF();
    keyspace = in.readUTF();
    columnFamily = in.readUTF();
    splitSize = in.readInt();
    rangeBatchSize = in.readInt();
    slicePredicateSize = in.readInt();
    partitioner = in.readUTF();
    port = in.readInt();
    host = in.readUTF();
    isIncremental = in.readBoolean();
    if (isIncremental) split = new IncrementalColumnFamilySplit();

    split.readFields(in);
  }

  @Override
  public void write(DataOutput out) throws IOException {
    super.write(out);
    out.writeUTF(columnMapping);
    out.writeUTF(keyspace);
    out.writeUTF(columnFamily);
    out.writeInt(splitSize);
    out.writeInt(rangeBatchSize);
    out.writeInt(slicePredicateSize);
    out.writeUTF(partitioner);
    out.writeInt(port);
    out.writeUTF(host);
    out.writeBoolean(isIncremental);
    split.write(out);
  }

  @Override
  public String[] getLocations() throws IOException {
    return split.getLocations();
  }

  @Override
  public long getLength() {
      try {
          return split.getLength();
      } catch (IOException e) {
          return -1;
      }
  }

  public String getKeyspace() {
    return keyspace;
  }

  public void setKeyspace(String keyspace) {
    this.keyspace = keyspace;
  }

  public String getColumnFamily() {
    return columnFamily;
  }

  public void setColumnFamily(String columnFamily) {
    this.columnFamily = columnFamily;
  }

  public int getRangeBatchSize() {
    return rangeBatchSize;
  }

  public void setRangeBatchSize(int rangeBatchSize) {
    this.rangeBatchSize = rangeBatchSize;
  }

  public int getSlicePredicateSize() {
    return slicePredicateSize;
  }

  public void setSlicePredicateSize(int slicePredicateSize) {
    this.slicePredicateSize = slicePredicateSize;
  }

  public org.apache.hadoop.mapreduce.InputSplit getSplit() {
    return (org.apache.hadoop.mapreduce.InputSplit)split;
  }

  public String getColumnMapping() {
    return columnMapping;
  }

  public void setColumnMapping(String mapping){
    this.columnMapping=mapping;
  }

  public void setPartitioner(String part){
    partitioner = part;
  }

  public String getPartitioner(){
    return partitioner;
  }

  public int getPort(){
    return port;
  }

  public void setPort(int port){
    this.port = port;
  }

  public String getHost(){
    return host;
  }

  public void setHost(String host){
    this.host = host;
  }

  @Override
  public String toString(){
    return this.host+" "+this.port+" "+this.partitioner;
  }

  public void setSplitSize(int splitSize) {
    this.splitSize = splitSize;
  }

  public int getSplitSize() {
    return splitSize;
  }
}
