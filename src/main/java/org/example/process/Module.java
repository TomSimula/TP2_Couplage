package org.example.process;

public class Module {
    private String name;
    private Module leftModule;
    private Module rightModule;

    public Module(String name){
        this.name = name;
        leftModule = null;
        rightModule = null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Module getLeftModule() {
        return leftModule;
    }

    public void setLeftModule(Module leftModule) {
        this.leftModule = leftModule;
    }

    public Module getRightModule() {
        return rightModule;
    }

    public void setRightModule(Module rghtModule) {
        this.rightModule = rghtModule;
    }

    public String toString(int indentation) {
        String res = addIndentation(indentation);
        res += name + "\n";
        if (leftModule != null){
            res += leftModule.toString(indentation+1);
        }
        if (rightModule != null){
            res += rightModule.toString(indentation+1);
        }
        return res;
    }

    private String addIndentation(int nb){
        String res = "";
        for (int i = 0; i < nb; i++) {
            res += "\t";
        }
        return res;
    }

    @Override
    public String toString() {
        int indentation = 2;
        return this.toString(2);
    }
}
