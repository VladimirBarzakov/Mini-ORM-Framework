package orm.entity_scanner;

import annotations.Entity;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class ClassEntityScanner {
    private Map<String, Class> foundEntities = new HashMap<>();

    public Map<String, Class> listFilesForFolder(String classpath) {
        LinkedList<File> directoryQueue = new LinkedList<>();
        File currFolder = new File(classpath);
        directoryQueue.addFirst(currFolder);

        String test = "entities.User";

        while (!directoryQueue.isEmpty()){
            currFolder=directoryQueue.removeLast();
            Loop:
            for (File file : currFolder.listFiles()) {
                if (file.isDirectory()){
                    this.addSubfolderToQueue(file,directoryQueue);
                }else{
                    if (file.isFile()&&file.getAbsolutePath().endsWith(".class")){
                        String currPath = file.getAbsolutePath().replace('\\','.').replace(".class","");
                        Class clazz =null;
                        do{
                            try {
                                clazz = Class.forName(currPath);
                                if (!clazz.isAnnotationPresent(Entity.class)){
                                    continue Loop;
                                }
                                Constructor<?> constructor = clazz.getDeclaredConstructor();
                                Object instance = constructor.newInstance();
                                if (instance.getClass().isAnnotationPresent(Entity.class)){
                                    this.foundEntities.put(instance.getClass().getSimpleName(),instance.getClass());
                                }
                            } catch (Exception e) {
                                currPath=this.doShortPath(currPath);
                            }
                        }while (clazz==null&&!currPath.equals(""));

                    }
                }
            }
        }
        return this.foundEntities;
    }

    private String doShortPath(String currPath) {
        int firstIndex = currPath.indexOf('.');
        return currPath.substring(firstIndex+1);
    }

    private void addSubfolderToQueue(File directory, LinkedList<File> directoryQueue) {
        if (!directory.isDirectory()){
            return;
        }
        directoryQueue.addFirst(directory);
    }

    public Map<String, Class> getFoundEntities() {
        return this.foundEntities;
    }
}
