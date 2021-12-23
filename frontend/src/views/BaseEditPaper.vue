<template>
  <v-card
    calss="rounded-lg"
    elevation=6
    width=100%
  >
    <div class="whole">
          <v-row v-if="edit">
            <v-col >
              <v-textarea
                label="修改原因和描述"
                v-model="discription"
              ></v-textarea>
            </v-col>
          </v-row>
          <v-row>
            <v-col>
              <v-text-field
                v-model="title"
                label="标题"
                required
              ></v-text-field>
            </v-col>
          </v-row>
          <v-row>
            <v-col >
              <v-textarea
                label="摘要"
                v-model="abstract"
              ></v-textarea>
            </v-col>
          </v-row>
          <v-row>
            <v-col v-for="i in keywords.length" :key="i" cols="6">
              <v-row>
                <v-col>
                  <v-text-field
                    label="关键词"
                    v-model="keywords[i-1]"
                  ></v-text-field>
                </v-col>
                <v-col>
                  <v-btn @click="deleteKeyword(i-1)">
                    删除该项
                  </v-btn>
                </v-col>
              </v-row>
            </v-col>
          </v-row>
          <v-row>
            <v-col>
              <v-btn @click="addKeyword()">
                添加关键词
              </v-btn>
            </v-col>
          </v-row>
          <v-row>
            <v-col v-for="i in subjects.length" :key="i" cols="6">
              <v-row>
                <v-col>
                  <v-text-field
                    label="学科分类"
                    v-model="subjects[i-1]"
                  ></v-text-field>
                </v-col>
                <v-col>
                  <v-btn @click="deleteSubject(i-1)">
                    删除该项
                  </v-btn>
                </v-col>
              </v-row>
            </v-col>
          </v-row>
          <v-row>
            <v-col>
              <v-btn @click="addSubject()">
                添加学科分类
              </v-btn>
            </v-col>
          </v-row>
          <v-row>
            <v-select
              :items="types"
              label="文献类型"
              v-model="type"
              solo
            ></v-select>
          </v-row>
          <v-row v-for="i in authors.length" :key="i">
            <v-col cols="3">
            <br/>
              <v-btn @click="find('科研人员','aut'+(i-1))">
                查找作者
              </v-btn>
            </v-col>
            <v-col cols="3">
              <v-switch v-model="editAut[i-1]" label='自行编辑作者信息'></v-switch>
            </v-col>
            <v-col cols="3">
              <v-text-field
                v-model="authors[i-1].name"
                label="作者姓名"
                :disabled="!editAut[i-1]"
                required
              ></v-text-field>
            </v-col>
            <v-col cols="3">
              <v-text-field
                v-model="authors[i-1].instName"
                label="作者所在机构名"
                :disabled="!editAut[i-1]"
                required
              ></v-text-field>
            </v-col>
            <v-col cols="2">
              <v-btn @click="deleteAuthor(i-1)">
                删除该项
              </v-btn>
            </v-col>
          </v-row>
          <v-btn @click="addAuthor()">
            添加作者
          </v-btn>
          <v-row>
            <v-col>
              <p>发表日期:</p>
              <v-date-picker v-model="date"></v-date-picker>
            </v-col>
          </v-row>
          
          <v-row>
            <v-col >
              <v-text-field
                label="DOI"
                v-model="doi"
              ></v-text-field>
            </v-col>
          </v-row>
          <v-row v-for="i in institutions.length" :key="i">
            <v-col cols="3">
              <br/>
                <v-btn @click="find('机构','ins'+(i-1))">
                  查找机构
                </v-btn>
              </v-col>
              <v-col cols="3">
                <v-switch v-model="editIns[i-1]" label='自行编辑机构名称'></v-switch>
              </v-col>
              <v-col cols="4">
                <v-text-field
                  v-model="institutions[i-1].name"
                  label="机构名称"
                  :disabled="!editIns[i-1]"
                  required
                ></v-text-field>
              </v-col>
            <v-col cols="2">
              <v-btn @click="deleteI(i-1)">
                删除该项
              </v-btn>
            </v-col>
          </v-row>
          <v-btn @click="addInst()">
            添加机构
          </v-btn>
          <v-row>
            <v-col cols="3">
            <br/>
              <v-btn @click="find('期刊','jou')">
                查找期刊
              </v-btn>
            </v-col>
            <v-col cols="3">
              <v-switch v-model="editCur" label='自行编辑期刊信息'></v-switch>
            </v-col>
            <v-col cols="6">
              <v-text-field
                label="期刊标题"
                v-model="journal.title"
                :disabled="!editJou"
              ></v-text-field>
            </v-col>
          </v-row>
          <v-row>
            <v-col cols="6">
              <v-text-field
                label="论文在期刊的期号"
                v-model="journal.issue"
              ></v-text-field>
            </v-col>
            <v-col cols="6">
              <v-text-field
                label="论文在期刊的卷号"
                v-model="journal.volume"
              ></v-text-field>
            </v-col>
          </v-row>
          <v-row>
            <v-col >
              <v-text-field
                label="论文在期刊中的起始页码"
                v-model="journal.startPage"
              ></v-text-field>
            </v-col>
            <v-col >
              <v-text-field
                label="论文在期刊中的终止页码"
                v-model="journal.endPage"
              ></v-text-field>
            </v-col>
          </v-row>
          <v-row>
            <v-col >
              <v-text-field
                label="出版商"
                v-model="publisher"
              ></v-text-field>
            </v-col>
          </v-row>
          <v-row v-for="i in referencePapers.length" :key="i">
            <v-col cols="3">
              <br/>
                <v-btn @click="find('论文','pap'+(i-1))">
                  查找论文
                </v-btn>
              </v-col>
              <v-col cols="3">
                <v-switch v-model="editPap[i-1]" label='自行编辑论文名称'></v-switch>
              </v-col>
            <v-col cols="5">
              <v-text-field
                v-model="referencePapers[i-1].title"
                label="引用文献标题"
                :disabled="!editPap[i-1]"
                required
              ></v-text-field>
            </v-col>
            <v-col cols="2">
              <v-btn @click="deleteReferencePapers(i-1)">
                删除该项
              </v-btn>
            </v-col>
          </v-row>
          <v-btn @click="addReferencePapers()">
            添加引用文献
          </v-btn>
          <v-row>
            <v-col >
              <v-text-field
                v-model="websiteLink"
                label="证明材料链接"
              ></v-text-field>
            </v-col>
          </v-row>
          <v-row>
            <v-col>
              <v-file-input 
              chips 
              label="上传证明文件"
              ></v-file-input>
            </v-col>
          </v-row>
          <v-row>
            <v-col v-if="edit" cols="6">
              <v-btn @click="submitEditPaper()">
                提交修改论文申请
              </v-btn>
            </v-col>
            <v-col v-else cols="6">
              <v-btn @click="submitAddPaper()">
                提交添加论文申请
              </v-btn>
            </v-col>
            <v-col cols="6">
              <v-btn @click="cancel()">
                取消
              </v-btn>
            </v-col>
          </v-row> 
      
     
    </div>


    <v-dialog v-model="getID" persistent width=1500px >
      <v-card height=5000px>
        <Search
          :key="timer"
          :fromDoor="disabled"
          :todo="todo"
          @closeID="closeID"
          @findResult="findResult"
        ></Search>
      </v-card>
    </v-dialog>
  </v-card>
</template>

<script>
  // import BaseGetID from './BaseGetID.vue'
  import Search from './Search.vue'
  export default {
    components: {Search},
    props:{
      paper:Object,
      edit:Boolean,
      email:String,
      portalid:String,
    },
    data(){
      return{
        title:this.paper.title,
        id:this.paper.id,
        abstract:this.paper.abstract,
        authors:this.paper.authors,
        date:this.paper.date,
        doi:this.paper.doi,
        institutions:this.paper.institutions,
        journal:this.paper.journal,
        keywords:this.paper.keywords,
        publisher:this.paper.publisher,
        referencePapers:this.paper.referencePapers,
        subjects:this.paper.subjects,
        type:this.paper.type,
        year:this.paper.year,
        types:["图书","学位论文","期刊论文"],
        close:"",
        discription:"",
        fileToken:null,
        websiteLink:null,
        getID:false,
        todo:"全部",
        disabled:"disabled",
        editCur:false,
        editJou:false,
        editIns:[],
        editAut:[],
        editPap:[],
        itemGetM:null,
        timer:"",
      }
    },
    methods:{
      addAuthor(){
        this.authors.push({
          id:"",
          name:"",
          instName:""
        })
        this.editAut.push(false)
      },
      deleteAuthor(index){
        this.authors.splice(index, 1)
        this.editAut.splice(index,1)
      },
      addKeyword(){
        this.keywords.push("")
      },
      deleteKeyword(index){
        this.keywords.splice(index, 1)
      },
      addSubject(){
        this.subjects.push("")
      },
      deleteSubject(index){
        this.subjects.splice(index, 1)
      },
      addReferencePapers(){
        this.referencePapers.push({
          id:"",
          title:""
        })
        this.editPap.push(false)
      },
      deleteReferencePapers(index){
        this.referencePapers.splice(index, 1)
        this.editPap.splice(index,1)
      },
      addInst(){
        this.institutions.push({
          id:"",
          name:""
        })
      },
      deleteI(index){
        this.institutions.splice(index, 1)
      },
      check(){
        if(this.title==''){
          this.$notify({
            title: "失败",
            message: "请输入标题",
            type: "error",
          });
          return false
        }else if(this.abstract==''){
          this.$notify({
            title: "失败",
            message: "请输入摘要",
            type: "error",
          });
          return false
        }else if(this.date==''){
          this.$notify({
            title: "失败",
            message: "请选择发表日期",
            type: "error",
          });
          return false
        }else if(this.authors.length==0){
          this.$notify({
            title: "失败",
            message: "请添加作者",
            type: "error",
          });
          return false
        }else if(this.institutions.length==0){
          this.$notify({
            title: "失败",
            message: "请添加机构",
            type: "error",
          });
          return false
        }else if(this.keywords.length==0){
          this.$notify({
            title: "失败",
            message: "请添加关键词",
            type: "error",
          });
          return false
        }else if(this.subjects.length==0){
          this.$notify({
            title: "失败",
            message: "请添加学科分类",
            type: "error",
          });
          return false
        }else if(this.websiteLink==null&&this.fileToken==null){
          this.$notify({
            title: "失败",
            message: "网址证明和文件证明至少需要一个",
            type: "error",
          });
          return false
        }else if(this.type==""){
          this.$notify({
            title: "失败",
            message: "请选择类型",
            type: "error",
          });
          return false
        }else{
          let i
          for(i=0;i<this.authors.length;i++){
            if(this.authors[i].id==this.portalid){
              break
            }
          }
          if(i==this.authors.length){
            this.$notify({
              title: "失败",
              message: "不能添加作者不包括自己的文献",
              type: "error",
            });
            return false
          }
        }
      },
      submitAddPaper(){
        if(this.check()==false){
          return
        }
        let data={
          content:{
            add:{
              abstract:this.abstract,
              authors:this.authors,
              date:this.date,
              doi:this.doi,
              institutions:this.institutions,
              journal:this.journal,
              keywords:this.keywords,
              publisher:this.publisher,
              subjects:this.subjects,
              referencePapers:this.referencePapers,
              title:this.title,
              type:this.type,
              year:this.date.substring(0,4),
            },
          },
          email:this.email,
          websiteLink:this.websiteLink,
          fileToken:this.fileToken
        }
        if(this.editJou){
          data.content.add.journal.id=null
        }else{
          data.content.add.journal.title=null
        }
        let i=0
        for(i=0;i<data.content.add.institutions.length;i++){
          if(this.editIns[i]){
            data.content.add.institutions[i].id=null
          }else{
            data.content.add.institutions[i].name=null
          }
        }
        for(i=0;i<data.content.add.authors.length;i++){
          if(this.editAut[i]){
            data.content.add.authors[i].id=null
          }else{
            data.content.add.authors[i].name=null
            data.content.add.authors[i].instName=null
          }
        }
        for(i=0;i<data.content.add.referencePapers.length;i++){
          if(this.editPap[i]){
            data.content.add.referencePapers[i].id=null
          }else{
            data.content.add.referencePapers[i].title=null
          }
        }
        console.log(JSON.stringify(data))
        this.$axios({
          method: "post",
          url: "/api/scholar/paper/add",
          data: data
        }).then(response => {
          console.log(response.data)
          if(response.data.success){
            // this.snackbarSub=true
            this.$emit('closeZ',"close")
          }
          
        }).catch(error => {
          console.log(error)
        })
      },
      submitEditPaper(){
        if(this.check==false){
          return
        }else if(this.description==''){
          this.$notify({
            title: "失败",
            message: "请输入修改原因或修改描述",
            type: "error",
          });
          return
        }
        let data={
          content:{
            discription:this.description,
            paperId:this.id,
            edit:{
              abstract:this.abstract,
              authors:this.authors,
              date:this.date,
              doi:this.doi,
              institutions:this.institutions,
              journal:this.journal,
              keywords:this.keywords,
              publisher:this.publisher,
              subjects:this.subjects,
              referencePapers:this.referencePapers,
              title:this.title,
              type:this.type,
              year:this.date.substring(0,4),
            }
          },
          email:this.email,
          websiteLink:this.websiteLink,
          fileToken:this.fileToken
        }
        if(this.editJou){
          data.content.edit.journal.id=null
        }else{
          data.content.edit.journal.title=null
        }
        let i=0
        for(i=0;i<data.content.edit.institutions.length;i++){
          if(this.editIns[i]){
            data.content.edit.institutions[i].id=null
          }else{
            data.content.edit.institutions[i].name=null
          }
        }
        for(i=0;i<data.content.edit.authors.length;i++){
          if(this.editAut[i]){
            data.content.edit.authors[i].id=null
          }else{
            data.content.edit.authors[i].name=null
            data.content.edit.authors[i].instName=null
          }
        }
        for(i=0;i<data.content.edit.referencePapers.length;i++){
          if(this.editPap[i]){
            data.content.edit.referencePapers[i].id=null
          }else{
            data.content.edit.referencePapers[i].title=null
          }
        }
        console.log(JSON.stringify(data))
        this.$axios({
          method: "post",
          url: "/api/scholar/paper/edit",
          data: data
        }).then(response => {
          console.log(response.data)
          if(response.data.success){
            // this.snackbarSub=true
            this.$emit('closeZ',"close")
          }
          
        }).catch(error => {
          console.log(error)
        })
      },
      cancel(){
        this.$emit('closeZ',"close")
      },
      closeID(msg){
        if(msg=="close")
          this.getID=false
      },
      findResult(msg){
        let str=this.itemGetM
        if(str.substring(0,3)=='pap'||str.substring(0,3)=='jou'){
          msg.title=msg.title.replaceAll('<b>','')
          msg.title=msg.title.replaceAll('</b>','')
        }else{
          msg.name=msg.name.replaceAll('<b>','')
          msg.name=msg.name.replaceAll('</b>','')
        }
        
        if(str=='cur'){
          this.currentInst=msg
        }else if(str.substring(0,3)=='ins'){
          let x=Number(str.substring(3))
          this.institutions[x]=msg
        }else if(str.substring(0,3)=='aut'){
          let x=Number(str.substring(3))
          this.authors[x].id=msg.id
          this.authors[x].name=msg.name
          this.authors[x].instName=msg.institution.name
        }else if(str.substring(0,3)=='pap'){
          let x=Number(str.substring(3))
          this.referencePapers[x].id=msg.id
          this.referencePapers[x].title=msg.title
        }else if(str.substring(0,3)=='jou'){
          this.journal.id=msg.id
          this.journal.title=msg.title
          
        }
        
      },
      find(type,it){
        this.todo=type
        this.getID=true
        this.itemGetM=it
        this.timer=new Date().getTime()
      }

    }
  }
</script>
<style>
.fixBut{
    position: fixed;
    top:60px;
    right:25%;
    z-index:9999;
}
</style>