DYNUCB: Dynamic Upper Confidence Bound
--------------------------------------

### INTRODUCTION: 

- This is an implementation in java for our paper "Dynamic Clustering of Contextual Multi-Armed Bandits" (Nguyen & Lauw, CIKM 2014)


The folder includes:
- Data: for importing the Delicious and LastFM database (using MYSQL(usr: 'root', pw: '') for connection).
- Input: the norm matrix after reducing dimensions.
- Output: the results is separated into N-tries (N threads) for taking the average.


*** Run from terminal:
java -Xmx4G -jar cikm.jar [args]

with arguments in order below:
- 1st: algorithm selection (1-LinUCBSIN; 2-LinUCBIND; 3-DynUCB)
- 2nd: dataset selection (1-delicious; 2-lastfm)
- 3rd: the number of running times/threads of the selected algorithm (e.g. 10)
- 4th: the number of iterations (e.g. 50000)
- 5th(optional): the number of clusters (default 16-clusters) in case of choosing DynUCB algorithm


### Examples
- Running DynUCB algorithm on Delicious dataset with 16 clusters, 10 threads, 50000 iterations.
java -Xmx4G -jar cikm.jar 3 1 10 50000 16


### HOW TO CITE:

If you use DynUCB in your research, please cite the paper with the bibtex format below:

@inproceedings{\
  &nbsp;&nbsp;&nbsp; nguyen2014dynamic,\
  &nbsp;&nbsp;&nbsp; title={Dynamic clustering of contextual multi-armed bandits},\
  &nbsp;&nbsp;&nbsp; author={Nguyen, Trong T and Lauw, Hady W},\
  &nbsp;&nbsp;&nbsp; booktitle={Proceedings of the 23rd ACM International Conference on Conference on Information and Knowledge Management},\
  &nbsp;&nbsp;&nbsp; pages={1959--1962},\
  &nbsp;&nbsp;&nbsp; year={2014},\
  &nbsp;&nbsp;&nbsp; organization={ACM}\
}
