package eu.alfu64.j.javadts;


import java.util.Arrays;
import java.util.stream.Collectors;

public class FqnInfo{
    public static FqnInfo of(Class<?> clazz){
        return new FqnInfo(clazz.getName());
    }
    String fqn;
    String[] pathTokens;
    String simpleTypeName;
    String parentFqn;
    String[] parentTokens;
    public FqnInfo(String fqn){

            switch (fqn){
                case "int":
                case "double":
                case "long":
                case "byte":
                case "char": this.fqn="number";break;
                default: this.fqn=fqn;break;
            }
        this.pathTokens=this.fqn.split("\\.");
        this.simpleTypeName = pathTokens.length==0?"":pathTokens[pathTokens.length-1];
        if(this.simpleTypeName.equals(this.fqn)){
            this.parentFqn = "";
            this.parentTokens = new String[]{};
        }else {
            this.parentFqn = this.fqn.substring(0, this.fqn.length() - simpleTypeName.length() - 1);
            if(parentFqn.equals("")){
                this.parentTokens = new String[]{};
            }else {
                this.parentTokens = parentFqn.split("\\.");
            }
        }
    }
    public String[] getPathTokens(){
        return pathTokens;
    }
    public String getPath(){
        return String.join("/",pathTokens).replaceAll("\\[\\]","");
    }
    public String getSimpleTypeName(){
        return simpleTypeName;
    }
    public String getSimpleTypeNameNoArray(){
        return simpleTypeName.replaceAll("\\[\\]","");
    }
    public String getParentFqn(){
        return parentFqn;
    }
    public String[] getParentTokens(){
        return parentTokens;
    }
    public String getPathToRoot(){
        if(parentTokens.length==0)return ".";
        return String.join("/", Arrays.stream(parentTokens).map(r -> "..").collect(Collectors.toList()));
    }
    public String getParentPath(){
        if(parentTokens.length==0)return ".";
        return String.join("/", parentTokens);
    }
    public FqnInfo getParentFqnInfo(){
        return new FqnInfo(parentFqn);
    }
    @Override
    public String toString(){
        return String.format(""+ "\n" +
        "{"+ "\n" +
        "              \"fqn\":\"%s\"," + "\n" +
        "       \"pathTokens\":%s," + "\n" +
        "   \"simpleTypeName\":\"%s\"," + "\n" +
        "        \"parentFqn\":\"%s\"," + "\n" +
        "     \"parentTokens\":%s" + "\n" +
        "}" + "\n" +
        "",
            fqn,
            "["+String.join(",",pathTokens)+"]",
            simpleTypeName,
            parentFqn,
            "["+String.join(",",parentTokens)+"]"
        );
    }
}
