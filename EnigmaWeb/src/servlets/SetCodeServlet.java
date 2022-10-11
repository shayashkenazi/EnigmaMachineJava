package servlets;

import DTOs.DTO_CodeDescription;
import DTOs.DTO_MachineInfo;
import EnginePackage.EngineCapabilities;
import WebConstants.Constants;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javafx.util.Pair;
import utils.ServletUtils;

import java.io.IOException;
import java.util.List;
import java.util.*;

@WebServlet(name = "SetCodeServlet", urlPatterns = {"/SetCodeServlet"})
public class SetCodeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String userName = request.getParameter(Constants.USERNAME);
        EngineCapabilities engine = ServletUtils.getBattlefield(getServletContext(), userName).getEngine();
        String setCodeType = request.getParameter(Constants.CODE_TYPE);

        switch (setCodeType) {
            case Constants.RANDOM_SET_CODE_TYPE:
                DTO_MachineInfo dtoMachineInfo = engine.createMachineInfoDTO();
                createRandomMachineSetting(dtoMachineInfo,engine);
                break;

            case Constants.SET_SPECIFIC_CODE_TYPE:

                break;
            default:
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                break;
        }
    }

private void createRandomMachineSetting(DTO_MachineInfo dtoMachineInfo,EngineCapabilities engine) {

        List<Pair<String , Pair<Integer,Integer>>> rotorsIDList = randomCreateIDListForRotors(dtoMachineInfo.getNumOfPossibleRotors(),dtoMachineInfo.getNumOfUsedRotors());
        List<Character>  startPositionList = randomCreateListForStartPosition(dtoMachineInfo,rotorsIDList,dtoMachineInfo.getABC(),rotorsIDList.size());

        String reflectorID = randomCreateReflectorID(dtoMachineInfo.getNumOfReflectors());
        List<Pair<Character, Character>> plugBoard = new ArrayList<>();
        DTO_CodeDescription res = new DTO_CodeDescription(dtoMachineInfo.getABC(),rotorsIDList,startPositionList,reflectorID,plugBoard);
        engine.buildRotorsStack(res, true);
        /*isCodeChosen.set(false);
        isCodeChosen.set(true);*/
    }
    private List<Pair<String ,Pair<Integer,Integer>>>  randomCreateIDListForRotors(int numOfRotors,int numOfUsedRotors) {
        List<Pair<String ,Pair<Integer,Integer>>>  rotorsIDList = new ArrayList<>();
        Random rand = new Random();
        Set<Integer > set = new HashSet<>();
        int randomNum;
        for(int i = 0; i < numOfUsedRotors; i++){
            randomNum = rand.nextInt(numOfRotors) + 1;
            while(set.contains(randomNum)) {
                randomNum = rand.nextInt(numOfRotors) + 1;
            }
            rotorsIDList.add(new Pair<>(String.valueOf(randomNum),null));
            set.add(randomNum);
        }
        return rotorsIDList;
    }
    private List<Character> randomCreateListForStartPosition(DTO_MachineInfo dto_machineInfo, List<Pair<String ,Pair<Integer,Integer>>> rotorsIDList, String abc, int numOfRotors) {
        List<Character> rotorsStartPositionList = new ArrayList<>();
        Set<Character> set = new HashSet<>();
        Random rand = new Random();
        int randomNum;
        for(int i = 0; i < numOfRotors; i++) {
            randomNum = rand.nextInt(abc.length());
            while(set.contains(abc.charAt(randomNum))) {
                randomNum = rand.nextInt(abc.length());
            }
            set.add(abc.charAt(randomNum));
            rotorsStartPositionList.add(abc.charAt(randomNum));
            Pair<String, Pair<Integer, Integer>> tmp = rotorsIDList.get(i);
            int curNotch = dto_machineInfo.getNotchPositionList().get(Integer.parseInt(tmp.getKey()) -1);
            rotorsIDList.set(i,new Pair<>(tmp.getKey(),new Pair<>(curNotch,dto_machineInfo.getABCOrderOfSpecificRotor(Integer.parseInt(tmp.getKey()) -1).indexOf(abc.charAt(randomNum)))));
        }
        return rotorsStartPositionList;
    }
    private String randomCreateReflectorID(int numOfReflectors) {
        Random rand = new Random();
        Map<Integer, String> MapNumbers = new LinkedHashMap<>();
        MapNumbers.put(1,"I");
        MapNumbers.put(2,"II");
        MapNumbers.put(3,"III");
        MapNumbers.put(4,"IV");
        MapNumbers.put(5,"V");
        return MapNumbers.get(rand.nextInt(numOfReflectors) + 1);

    }
}


