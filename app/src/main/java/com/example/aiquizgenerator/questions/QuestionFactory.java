package com.example.aiquizgenerator.questions;

import org.json.JSONArray;
import java.util.List;
import com.example.aiquizgenerator.helpers.RuleParser;

public class QuestionFactory {

    public static List<QuestionBank> generateQuestionsFromTokens(JSONArray posTaggedJson) {
        return RuleParser.generateQuestionsFromTokens(posTaggedJson);
    }
}
