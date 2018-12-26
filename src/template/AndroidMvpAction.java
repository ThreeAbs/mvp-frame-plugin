package template;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Author: Allen Wang
 * Email:  181628396@qq.com
 * Time:   &time&
 */

public class AndroidMvpAction extends AnAction {
    Project project;
    VirtualFile selectGroup;

    @Override
    public void actionPerformed(AnActionEvent e) {
        project = e.getProject();
        String className = Messages.showInputDialog(project, "请输入类名称，Fragment或Activity结尾", "生成Mvp文件", Messages.getQuestionIcon());
        selectGroup = DataKeys.VIRTUAL_FILE.getData(e.getDataContext());
        if (className == null || className.equals("")) {
            System.out.print("没有输入类名");
            return;
        }
        if (className.toLowerCase().equals("mvp")) {
            createMvpBase();
        } else {
            createClassMvp(className);
        }
        project.getBaseDir().refresh(false, true);
    }

    /**
     * 创建MVP的Base文件夹
     */
    private void createMvpBase() {
        String path = selectGroup.getPath() + "/mvp";
        String packageName = path.substring(path.indexOf("java") + 5, path.length()).replace("/", ".");
        System.out.println("log:" + selectGroup.getPath());
        //D:\AndroidWorkPlace\CuckcooMall\app\src\main\java\com\cuckoo\mall

        String presenter = readFile("BasePresenter.txt").replace("&package&", packageName);
        String presenterImpl = readFile("BasePresenterImpl.txt").replace("&package&", packageName);
        String view = readFile("BaseView.txt").replace("&package&", packageName);
        String activity = readFile("MVPBaseActivity.txt").replace("&package&", packageName);
        String fragment = readFile("MVPBaseFragment.txt").replace("&package&", packageName);

        writetoFile(presenter, path, "BasePresenter.java");
        writetoFile(presenterImpl, path, "BasePresenterImpl.java");
        writetoFile(view, path, "BaseView.java");
        writetoFile(activity, path, "MVPBaseActivity.java");
        writetoFile(fragment, path, "MVPBaseFragment.java");

    }

    /**
     * 创建MVP架构
     */
    private void createClassMvp(String className) {
        boolean isFragment = className.toLowerCase().endsWith("fragment");
        if (className.endsWith("Fragment") || className.endsWith("fragment") || className.endsWith("Activity") || className.endsWith("activity")) {
            className = className.substring(0, className.length() - 8);
        }
        className = className.substring(0, 1).toUpperCase() + className.substring(1);
        String path = selectGroup.getPath() + "/" + className.toLowerCase();
        String packageName = path.substring(path.indexOf("java") + 5, path.length()).replace("/", ".");

        /*Contract*/
        String contract = readAndReplaceString("Contract", packageName, className);
        writetoFile(contract, path, className + "Contract.java");
        /*Presenter*/
        String presenter = readAndReplaceString("Presenter", packageName, className);
        writetoFile(presenter, path, className + "Presenter.java");

        /*Activity或者fragment*/
        String typeName = isFragment ? "Fragment" : "Activity";
        String placeString1 = "";
        String placeString2 = "";
        if (isFragment) {//fragment强制实现initView
            placeString1 = "import android.view.LayoutInflater;\n" +
                    "import android.view.View;";
            placeString2 = "\n" +
                    "    @Override\n" +
                    "    protected View initView(LayoutInflater inflater) {\n" +
                    "        return null;\n" +
                    "    }";
        }
        String compment = readAndReplaceString("Compment", packageName, className).replace("&type&", typeName).replace("&placeholder1&", placeString1).replace("&placeholder2&", placeString2);
        writetoFile(compment, path, className + typeName + ".java");

    }

    private String readAndReplaceString(String fileName, String packageName, String className) {
        // String contract = readFile("Contract.txt").replace("&time&", time).replace("&package&", packageName).replace("&mvp&", mvpPath).replace("&Contract&", className + "Contract");
        String baseFilePath = "com.bgn.baseframe.base";

        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
        return readFile(fileName + ".txt")
                .replace("&time&", time)
                .replace("&package&", packageName)
                .replace("&mvp&", baseFilePath)
                .replace("&className&", className);
    }


    private String readFile(String filename) {
        InputStream in = null;
        in = this.getClass().getResourceAsStream("code/" + filename);
        String content = "";
        try {
            content = new String(readStream(in));
        } catch (Exception e) {
        }
        return content;
    }

    private void writetoFile(String content, String filepath, String filename) {
        try {
            File floder = new File(filepath);
            // if file doesnt exists, then create it
            if (!floder.exists()) {
                floder.mkdirs();
            }
            File file = new File(filepath + "/" + filename);
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] readStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[1024];
            int len = -1;
            while ((len = inStream.read(buffer)) != -1) {
                outSteam.write(buffer, 0, len);
                System.out.println(new String(buffer));
            }

        } catch (IOException e) {
        } finally {
            outSteam.close();
            inStream.close();
        }
        return outSteam.toByteArray();
    }

}
