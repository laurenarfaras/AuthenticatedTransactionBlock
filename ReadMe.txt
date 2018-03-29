Steps to run:

1. Run the make file using "make -f makefile"
2. Start the program using "java ledger"
3. You will initially start in non-interactive mode. Please press i/I for entering interactive mode.
4. The commands entered in full words are ignored for case. For example Interactive, interactive, interActive all result in the toggling of interactive/non-interactive mode.
5. The input UTXOs sum amount and output UTXOs sum amount are supposed to be equal for the scope of my code. This is because of the assumption that the last output UTXO is the commission and hence both the amounts should be equal. Else, the transaction was deemed bad.

No identified bugs in the implementation.