package nl.framegengine.editor.panels;

import imgui.ImGui;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiSelectableFlags;
import nl.framegengine.editor.EditorPanel;
import nl.framegengine.editor.EngineSettings;
import nl.framegengine.core.debugging.Debug;
import nl.framegengine.core.utils.FileHelper;
import nl.framegengine.editor.ImGuiHelper;

import java.io.File;
import java.io.IOException;

public class ProjectPanel extends EditorPanel {
    public ProjectPanel(int posX, int posY, int sizeX, int sizeY) {
        super(posX, posY, sizeX, sizeY);
    }

    @Override
    public void renderFrame() {
        ImGui.setWindowFontScale(1.2f);
        if(!new File(EngineSettings.currentProjectDirectory).exists()) return;
        printFolderStructure(EngineSettings.currentProjectDirectory, EngineSettings.currentProjectName, 0);
        ImGuiHelper.showInputField();
    }

    private void printFolderStructure(String dir, String folderName, int level){

        File[] projectFoldersAndFiles = FileHelper.listDirectoryAndFiles(dir);

        boolean headerCollapsed = ImGui.collapsingHeader(folderName);

        if (ImGui.isItemHovered() && ImGui.isMouseReleased(ImGuiMouseButton.Right)) {
            ImGui.openPopup(dir + folderName);
        }

        showContextMenu(dir + folderName, dir);

        if(!headerCollapsed) return;

        ImGui.indent(10 * level);
        for (File projectFoldersAndFile : projectFoldersAndFiles) {
            if (projectFoldersAndFile.getName().startsWith(".")) continue;
            if (projectFoldersAndFile.isDirectory()) {
                printFolderStructure(projectFoldersAndFile.getAbsolutePath(), projectFoldersAndFile.getName(), level + 1);
            } else {
                ImGui.selectable(projectFoldersAndFile.getName(), false, ImGuiSelectableFlags.AllowDoubleClick);

                if(ImGui.isItemHovered()){
                    if(ImGui.isMouseDoubleClicked(ImGuiMouseButton.Left)) {
                        selectFile(projectFoldersAndFile);
                    }
                    if(ImGui.isMouseReleased(ImGuiMouseButton.Right)){
                        ImGui.openPopup(projectFoldersAndFile.getAbsolutePath());
                    }
                }

                showContextMenu(projectFoldersAndFile.getAbsolutePath(), projectFoldersAndFile.getAbsolutePath());
            }
        }
        ImGui.unindent(10 * level);
    }

    private void showContextMenu(String stringID, String path){
        if (ImGui.beginPopupContextItem(stringID)) {
            ImGui.text("Options");
            ImGui.newLine();
            if (ImGui.beginMenu("Create new")) {
                if (ImGui.menuItem("Folder")) {
                    ImGuiHelper.setInputFieldModal(name -> createNewDirectory(FileHelper.getDirectoryPath(path), name));
                    ImGui.closeCurrentPopup();
                }
                ImGui.text("--------");
                if (ImGui.menuItem("Class")) {
                    ImGuiHelper.setInputFieldModal(name -> createNewClass(FileHelper.getDirectoryPath(path), name));
                    ImGui.closeCurrentPopup();
                }
                if (ImGui.menuItem("Other file")) {
                    ImGuiHelper.setInputFieldModal(name -> createNewFile(FileHelper.getDirectoryPath(path), name));
                    ImGui.closeCurrentPopup();
                }
                ImGui.endMenu();
            }
            if(ImGui.menuItem("Open directory")){
                try {
                    FileHelper.openDirectory(new File(path));
                } catch (IOException e) {
                    Debug.Log("Cannot open directory: " + e.getMessage());
                }

                ImGui.closeCurrentPopup();
            }
            if (ImGui.menuItem("Delete")) {
                FileHelper.deleteFile(new File(path));
                ImGui.closeCurrentPopup();
            }
            ImGui.endPopup();
        }
    }

    private void selectFile(File selectedFile){
        Debug.Log("Selecting " + selectedFile.getName());
        try {
            String extension = FileHelper.getExtension(selectedFile.getName());
            switch (extension){
                case "lvl":
                    Debug.Log("Loading level " + selectedFile.getName());
                    EngineSettings.currentLevelPath = new File(EngineSettings.currentProjectDirectory).toURI().relativize(selectedFile.toURI()).getPath();
                    EngineSettings.saveSettings();
                    break;
                default:
                    FileHelper.openFile(selectedFile);
                    break;
            }
        } catch (IOException e) {
            Debug.LogError("Cannot open file: " + e.getMessage());
        }
    }

    private void createNewFile(String path, String fileName){
        FileHelper.writeToFile("", new File(path, fileName).getAbsolutePath());
    }

    private void createNewDirectory(String path, String directoryName){
        new File(path, directoryName).mkdirs();
    }

    private void createNewClass(String path, String className){
        try {
            String classTemplate = FileHelper.loadResource("/templates/default_class.java");
            classTemplate = classTemplate.replaceAll("CLASSNAME", className);
            FileHelper.writeToFile(classTemplate, new File(path, (className + ".java")).getAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
