import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.*;

/**
 * WordCount
 *
 * @author hengyumo
 * @version 1.0
 * @since 2019/11/17
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class WordCount {

    // 单词所需最小开头字母数
    private static final int MIN_WORD_LETTER_NUM = 4;

    // 行数
    private int lineNum = 0;

    // 字符数
    private int charNum = 0;

    // 单词数：至少四个英文字母开头，不区分大小写
    private int wordNum = 0;

    // 单词集合：<单词, 数目>
    private Map<String, Integer> wordMap = new TreeMap<>();

    // 排序好的单词集合
    private List<Map.Entry<String, Integer>> wordList = null;

    // 字节数组
    private byte[] bytes = null;

    // 词组词数
    private int phraseWordNum = 1;

    // 排序打印出前几的个数
    private int sortedPrintNum = 10;

    // 输入文件名
    private String inputFileName = null;

    // 输出文件名
    private String outputFileName = null;


    /**
     * 解析命令行参数
     *
     * @param args 命令行参数数组
     */
    public void loadArgs(String[] args){
        if (args != null && args.length > 0){
            for (int i = 0; i < args.length; i++){
                switch (args[i]){
                    case "-i":
                        inputFileName = args[++i];
                        break;
                    case "-o":
                        outputFileName = args[++i];
                        break;
                    case "-m":
                        phraseWordNum = Integer.valueOf(args[++i]);
                        break;
                    case "-n":
                        sortedPrintNum = Integer.valueOf(args[++i]);
                        break;
                    default:
                        break;
                }
            }
        } else{
            System.out.println("未输入参数");
            System.exit(1);
        }
       // System.out.println(String.format(
       //     "inputFileName:%s\noutputFileName:%s\nphraseWordNum:%s\nsortedPrintNum:%s\n",
       //     inputFileName, outputFileName, phraseWordNum, sortedPrintNum));
    }

    /**
     * 程序启动前校验运行参数是否正确
     */
    public void checkArgs(){
        if (inputFileName == null ||
                !(new File(inputFileName).exists())){
            System.out.println("输入文件不存在");
            System.exit(1);
        }
    }

    /**
     * 读取文件到字节数组中
     *
     * @param fileName 文件名
     *
     * @return bytes 字节数组
     */
    public byte[] readFileToBytes(String fileName){
        byte[] fileBytes = null;

        try {
            File file = new File(fileName);
            if (file.isFile() && file.exists()){
                FileInputStream reader = new FileInputStream(file);
                Long fileLength = file.length();
                // System.out.println("fileLength: " + fileLength);
                fileBytes = new byte[fileLength.intValue()];
                reader.read(fileBytes);
                reader.close();
            }
        }
        catch(FileNotFoundException e){
            System.out.println("文件不存在");
        }
        catch(Exception e){
            System.out.println("读取文件出错");
            e.printStackTrace();
        }

        return fileBytes;
    }

    /**
     * 过滤掉字节数组中的非ascii码字符
     *
     * @param bytes 字节数组
     *
     * @return noAsciiBytes
     */
    public byte[] filterAscii(byte[] bytes){
        byte[] noAsciiBytesTemp = new byte[bytes.length];
        int j = 0;
        for (int i = 0; i < bytes.length; i++){
            if (bytes[i] < 128 && bytes[i] >= 0){
                noAsciiBytesTemp[j++] = bytes[i];
            }
        }
        byte[] noAsciiBytes = new byte[j];
        System.arraycopy(noAsciiBytesTemp, 0, noAsciiBytes, 0, j);
        return noAsciiBytes;
    }

    /**
     * 预处理
     *      将大写字母转为小写字母
     *      计算字节数组中的字符数、包括空字符、//r//n算作一个字符
     *      兼容\n换行
     *      计算字节数组包含的行数
     *
     * @param bytes 字节数组
     */
    public void preProcess(byte[] bytes){
        if (bytes.length == 0) {
            return;
        }
        // 计算字符数、行数
        for (int i = 0; i < bytes.length; i ++){
            // 预处理，大写字母统一转为小写字母，同时过滤非ascii码字符
            if (bytes[i] >= 0 && bytes[i] < 128){
                if (bytes[i] >= 65 && bytes[i] <= 90){
                    bytes[i] += 32;
                }
                if (bytes[i] == 10){
                    // 计算行数
                    if (checkLine(bytes, i)){
                        lineNum ++;
                    }
                    // 当换行为\n时保证不遗漏字符
                    if (i-1 >= 0 && bytes[i-1] != 13){
                        charNum ++;
                    }
                }else{
                    charNum ++;
                }
            }
        }
        // 注意最后一行不以回车结尾的情况，同样算作一行
        if (bytes[bytes.length-1] != 10 && checkLine(bytes, bytes.length-1)){
            lineNum ++;
        }
    }

    /**
     * 判断该换行字符所在行是否是非空白行
     *
     * @param bytes 字节数组
     * @param lineEnd 换行符下标（行末尾）
     *
     * @return true 非空行 fasle 是空行
     */
    public boolean checkLine(byte[] bytes, int lineEnd){
        int notBlankCharNum = 0;
        for (int i = lineEnd-1; i >= 0; i --){
            if (bytes[i] == 10){
                // 遇到前一行返回
                break;
            } else if (!isBlankChar(bytes[i])){
                // 当前字母不是空格或制表符
                notBlankCharNum ++;
            }
        }
        return (notBlankCharNum > 0);
    }

    /**
     * 判断byte字节是否是字母
     *
     * @param b 字节
     *
     * @return true 是字母 false 不是字母
     **/
    public boolean isLetter(byte b){
        return (b >= 97 && b <= 122) || (b >= 65 && b <= 90);
    }

    /**
     * 判断Byte字节是否是数字
     *
     * @param b 字节
     *
     * @return true 是数字 false 不是数字
     */
    public boolean isNum(byte b){
        return (b >= 48 && b <= 57);
    }

    /**
     * 判断byte字节是否是空白字符
     *
     * @param b 字节
     *
     * @return true 是空白字符 false 不是空白字符
     **/
    public boolean isBlankChar(byte b){
        return (b <= 32 || b == 127);
    }

    /**
     * 判断Byte字节是否是分隔符
     *
     * @param b 字节
     *
     * @return true 是分隔符 false 不是分隔符
     */
    public boolean isSeparator(byte b){
        return !(isLetter(b)|| isNum(b));
    }

    /**
     * 计算单词/词组数、并将单词/词组装入集合、统计个数
     *
     * @param bytes 字节数组
     * @param wordWeight 权重
     */
    public void collectWord(byte[] bytes, int wordWeight){
        int bytesLength = bytes.length;
        int checkWordResult;
        String wordString;
        for (int i = 0; i < bytesLength; i ++){
            if (isLetter(bytes[i])){
                checkWordResult = checkWord(bytes, i, MIN_WORD_LETTER_NUM);
                if (checkWordResult > 0){
                    if (phraseWordNum == 1){
                        wordString = subBytesToString(bytes, i, checkWordResult);
                    } else {
                        wordString = checkPhrase(bytes, i, MIN_WORD_LETTER_NUM, phraseWordNum);
                    }
                    // System.out.println(wordString);

                    if (wordString != null){
                        // 如果构成词组，存入集合中
                        if (wordMap.containsKey(wordString)){
                            wordMap.put(wordString, wordMap.get(wordString)+(wordWeight));
                        } else{
                            wordMap.put(wordString, wordWeight);
                        }
                    }
                    wordNum ++;
                    // 跳转单词末尾
                    i = checkWordResult;
                } else{
                    // 不是单词，但是同样跳转到词末尾
                    i = - checkWordResult;
                }
                // System.out.println(checkWordResult);
            }
        }
    }

    /**
     * 按照单词频率排序
     */
    public void sortWordMap(){
        wordList = new ArrayList<>(wordMap.entrySet());
        wordList.sort((word1, word2) -> word2.getValue() - word1.getValue());
    }

    /**
     * 取出字节数组中的某一段转成String返回
     *
     * @param bytes 字节数组
     * @param start 开始下标
     * @param end 截止下标
     *
     * @return aWordString 截取转成的字符串
     */
    public String subBytesToString(byte[] bytes, int start, int end){
        if (end > start){
            byte[] aWordByte = new byte[end-start];
            System.arraycopy(bytes, start, aWordByte, 0, end-start);
            return new String(aWordByte);
        }
        return null;
    }

    /**
     * 判断从某个下标开始的一段长度是否是单词
     *
     * @param bytes 字节数组
     * @param start 开始下标
     * @param minWordLength 满足最小需求的开头字母数
     *
     * @return int < 0 不是单词，负的词末尾分隔符的下标
     *      int > 0 是单词，单词末尾分隔符的下标
     **/
    public int checkWord(byte[] bytes, int start, int minWordLength){
        int bytesLength = bytes.length;
        int i = start;
        int checkWordResult = 0;

        if (start > 0 && ! isSeparator(bytes[start-1])){
            checkWordResult = -1;
        } else{
            for (; i < start + minWordLength && i < bytesLength; i++){
                // 满足最小需求的开头字母数
                if (! isLetter(bytes[i])){
                    checkWordResult = -2;
                    break;
                }
            }
            // 已到结尾，不满足最小开头字母数
            if (i == bytesLength && i - start < minWordLength){
                checkWordResult = -3;
            }
        }
        for (; i < bytesLength; i++){
            // 遍历到词结束，返回词末尾的下标
            if (isSeparator(bytes[i])){
                // 字符不是分隔符
                break;
            }
        }

        return checkWordResult < 0 ? -i : i;
    }

    /**
     * 判断从某个下标开始的一段长度是否能构成所要求的词组长度
     *
     * @param bytes 字节数组
     * @param start 开始下标
     * @param minWordLength 满足最小需求的开头字母数
     * @param phraseWordNum 词组所需的词数
     *
     * @return null 不能构成词组（不合法字符或者词数不足）
     *       String 能构成词组，返回词组
     */
    public String checkPhrase(byte[] bytes, int start, int minWordLength, int phraseWordNum){
        int bytesLength = bytes.length;
        int nowIndex = start;
        int checkWordResult;
        for (int i = 0; nowIndex < bytesLength && i < phraseWordNum; i++){
            checkWordResult = checkWord(bytes, nowIndex, minWordLength);
            if (checkWordResult > 0){
                String aWordString = subBytesToString(bytes, nowIndex, checkWordResult);
                // System.out.println(aWordString);
                // 跳转单词末尾
                nowIndex = checkWordResult;
                // 判断是否是换行分隔符(\r\n或者\n)，如是，无法构成词组
                if ((i < phraseWordNum-1) && (nowIndex < bytesLength-1 && bytes[nowIndex] == 10 ||
                        (nowIndex < bytesLength-1 && bytes[nowIndex + 1] == 10))){
                    // System.out.println("遇到换行");
                    return null;
                }
                // 跳过分隔符、分隔符可能不止一个，但一定没有\n \r\n
                // if (i != phraseWordNum-1){
                //     while (nowIndex < bytesLength && isSeparator(bytes[nowIndex])){
                //         if (bytes[nowIndex] == 10){
                //             return null;
                //         }
                //         nowIndex ++;
                //     }
                // }
                ////////////////////////////
                // 修改为空格分隔词组
                // 如果分隔符不是一个空格则不是词组
                if (i != phraseWordNum-1){
                    while (nowIndex < bytesLength && isSeparator(bytes[nowIndex])){
                        if (bytes[nowIndex] != ' '){
                            return null;
                        }
                        nowIndex ++;
                    }
                }
            } else{
                // System.out.println("不是单词，无法构成词组");
                return null;
            }
            if (i != phraseWordNum-1 && nowIndex == bytesLength){
                return null;
            }
        }
        // 满足构成单词条件，返回词组
        return subBytesToString(bytes, start, nowIndex);
    }

    /**
     * 打印结果到控制台
     */
    public void printResult(){
        System.out.println("characters: " + charNum);
        System.out.println("words: " + wordNum);
        System.out.println("lines: " + lineNum);
        int i = 0;
        for (Map.Entry<String, Integer> entry : wordList) {
            System.out.println("<" + entry.getKey() + ">: " + entry.getValue());
            if (++ i >= sortedPrintNum){
                break;
            }
        }
    }
    /**
     * 输出结果到文件中
     */
    public void writeResult(){
        StringBuilder resultString = new StringBuilder(String.format(
                "characters: %d\nwords: %d\nlines: %d\n",
                charNum, wordNum, lineNum
        ));
        int i = 0;
        for (Map.Entry<String, Integer> entry : wordList) {
            resultString.append(String.format("<%s>: %d\n", entry.getKey(), entry.getValue()));
            if (++ i >= sortedPrintNum){
                break;
            }
        }
        try{
            FileWriter writer = new FileWriter(outputFileName, false);
            writer.write(resultString.toString());
            writer.close();
        }catch(Exception e){
            System.out.println("写入文件出错");
            e.printStackTrace();
        }
    }

    /**
     * 初始化
     *
     * @param args 命令行参数
     */
    public void init(String[] args) {
        // 初始化
        loadArgs(args);
        checkArgs();
        bytes = filterAscii(readFileToBytes(inputFileName));
        preProcess(bytes);
        // 长度：字节数组长度
        int bytesLength = bytes.length;
    }

    /**
     * 执行统计
     */
    public void count() {
        collectWord(bytes, 1);
        sortWordMap();
    }

    /**
     * 主程序
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {

        WordCount wordCount = new WordCount();
        wordCount.init(args);
        wordCount.count();
        wordCount.writeResult();
    }
}
