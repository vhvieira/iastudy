#!/usr/bin/env python3
import pandas as pd
questions = []
answers = []
with open('faq.txt') as ifp:
    for line in ifp.readlines():
        pieces = line.strip().split(':')
        if len(pieces) >= 2:
            term = pieces[0]
            definition = ':'.join(pieces[1:]).strip()
            if len(definition) > 10:
                questions.append("{}".format(term))
                answers.append(definition)
df = pd.DataFrame.from_dict({
    'question': questions,
    'answer': answers
})
df.to_csv('knowledge_base.csv', index=False, header=False)