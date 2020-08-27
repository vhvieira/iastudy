          #Method to send message
    def ReadMessages(self):
        print('Iniciando read messages')
        self.LoadConfiguration()
        self.driver.get(self.url)
        time.sleep(self.loadSleep)
        leftPane = self.driver.find_element_by_xpath(self.xPathForLefPane)
        participantList = leftPane.find_element_by_xpath(self.xPathForConversation)
        print('Lista de conversas encontrada')
        #Infinity loop for reading new messages
        print('Loop para leitura de mensagens...')
        #Finding for the listening phone list
        for dest in self.to:
            try:
                print('Reading novas conversas para:' + dest)
                conversations = participantList.find_elements_by_xpath(self.xPathForListener.replace("{name}", dest))
                print('Conversas encontradas: ' + str(len(conversations)))
                time.sleep(self.smallInterval)
                for conversation in conversations:
                    print('Conversa encontrada --> ' + conversation.text)
                    ##TODO: Starts from new message them try to find the title or do ../../ click
                    newConversation = conversation.find_element_by_xpath(self.xPathForNonRead.replace("{name}", dest))
                    print('Nova conversa: ' + str(newConversation) + ' - txt: ' + newConversation.text)
                    conversation.click()
                    time.sleep(self.smallInterval)
                    conversationDiv = self.driver.find_element_by_xpath("//div[@id='main']")
                    print('Conversa encontrada, buscando textos')
                    time.sleep(self.smallInterval)
                    #TODO This should be a functional call (reading texts)
                    texts = conversationDiv.find_elements_by_xpath(".//*[contains(@data-pre-plain-text,'27/07/2020')]")
                    for text in texts:
                        print( html2text.html2text(text.get_attribute("data-pre-plain-text")) + html2text.html2text(text.text) )
                        #TODO Dialogflow and no more echo service
                            #[0] is search in the conversation, [1] is chatbox
                        chatboxes = self.driver.find_elements_by_class_name(self.inputClass)
                        print('Chatboxs encontrados: ' + str(len(chatboxes)))
                        chatboxes[1].clear()
                        chatboxes[1].send_keys(html2text.html2text(text.text))
                        chatboxes[1].click()
            except Exception as ex:
                print('Não foram encontradas novas mensagens', ex)
            time.sleep(self.longInterval)